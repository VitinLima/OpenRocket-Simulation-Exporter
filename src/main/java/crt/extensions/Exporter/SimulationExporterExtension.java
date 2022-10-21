/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crt.extensions.Exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.unit.Unit;

/**
 *
 * @author 160047412
 */
public class SimulationExporterExtension extends AbstractSimulationExtension {

  public File outputFile;
  public String fieldSeparator = ",";
  public String commentCharacter = "#";
  public boolean includeSimulationDescription = true;
  public boolean includeFieldsDescription = true;
  public boolean includeFlightEvents = true;
  public int appendSelection = 0;
  public int[] fieldSelection = new int[]{};

  @Override
  public void initialize(SimulationConditions sc) throws SimulationException {
    sc.getSimulationListenerList().add(createListener());
  }

  void setOutputFile(File selectedFile) {
    this.outputFile = selectedFile;
  }

  public SimulationExporterListener createListener(){
    return new SimulationExporterListener(
            outputFile,
            fieldSeparator,
            commentCharacter,
            includeSimulationDescription,
            includeFieldsDescription,
            includeFlightEvents,
            appendSelection,
            fieldSelection);
  }
  
  public void exportCSV(FileWriter fw, Simulation simulation) throws IOException{
    FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
    FlightDataType[] fields = new FlightDataType[fieldSelection.length];
    Unit[] units = new Unit[fieldSelection.length];
    for(int i : fieldSelection){
      FlightDataType f = FlightDataType.ALL_TYPES[i];
      fields[i] = f;
      units[i] = f.getUnitGroup().getDefaultUnit();
    }

    fw.append("<title>");
    writeTitleToFOS(fw, simulation);
    fw.append(System.lineSeparator());
    fw.append("<\\title>");
    fw.append(System.lineSeparator());
    fw.append("<header>");
    writeHeaderToFOS(fw, simulation, branch, fields);
    fw.append(System.lineSeparator());
    fw.append("<\\header>");
    fw.append(System.lineSeparator());
    fw.append("<conditions,9>");
    writeConditionsToFOS(fw, simulation);
    fw.append(System.lineSeparator());
    fw.append("<\\conditions>");
    fw.append(System.lineSeparator());
    fw.append("<events," + branch.getEvents().size() + ">");
    writeEventsToFOS(fw, branch);
    fw.append(System.lineSeparator());
    fw.append("<\\events>");
    fw.append(System.lineSeparator());
    fw.append("<warnings," + simulation.getSimulatedWarnings().size() + ">");
    writeWarningsToFOS(fw, simulation);
    fw.append(System.lineSeparator());
    fw.append("<\\warnings>");
    fw.append(System.lineSeparator());
    fw.append("<variables," + fields.length + ">");
    writeVariablesToFOS(fw, fields, units);
    fw.append(System.lineSeparator());
    fw.append("<\\variables>");
    fw.append(System.lineSeparator());
    fw.append("<data," + branch.getLength() + ">");
    writeDataToFOS(fw, branch, fields);
    fw.append(System.lineSeparator());
    fw.append("<\\data>");
  }
  
  private void writeTitleToFOS(FileWriter fw, Simulation simulation) throws IOException{
    fw.append(System.lineSeparator());
    fw.append(simulation.getName() + " (" + simulation.getStatus().toString() + ")");
  }
  private void writeHeaderToFOS(FileWriter fw, Simulation simulation, FlightDataBranch branch, FlightDataType[] fields) throws IOException{
    fw.append(System.lineSeparator());
    fw.append(commentCharacter + " " + branch.getLength() + " data points written for "
            + fields.length + " variables with "
            + branch.getEvents().size() + " events, and "
            + simulation.getSimulatedWarnings().size() + " warnings.");
  }
  private void writeConditionsToFOS(FileWriter fw, Simulation simulation) throws IOException{
    double simulationWSA = simulation.getSimulatedConditions().getWindSpeedAverage();
    double simulationWSDir = simulation.getSimulatedConditions().getWindDirection();
    double simulationWSDev = simulation.getSimulatedConditions().getWindSpeedDeviation();
    double simulationWTI = simulation.getSimulatedConditions().getWindTurbulenceIntensity();
    double simulationLRA = simulation.getSimulatedConditions().getLaunchRodAngle();
    double simulationLRD = simulation.getSimulatedConditions().getLaunchRodDirection();
    double simulationLRLon = simulation.getSimulatedConditions().getLaunchLongitude();
    double simulationLRLat = simulation.getSimulatedConditions().getLaunchLatitude();
    double simulationLRAlt = simulation.getSimulatedConditions().getLaunchAltitude();
    String simulatedConditions = commentCharacter + " Wind speed average: " + simulationWSA + System.lineSeparator()
            + commentCharacter + " Wind speed direction: " + Math.toDegrees(simulationWSDir) + System.lineSeparator()
            + commentCharacter + " Wind turbulence intensity: " + simulationWTI + System.lineSeparator()
            + commentCharacter + " Wind speed deviation: " + simulationWSDev + System.lineSeparator()
            + commentCharacter + " Launch rod angle: " + Math.toDegrees(simulationLRA) + System.lineSeparator()
            + commentCharacter + " Launch rod direction: " + Math.toDegrees(simulationLRD) + System.lineSeparator()
            + commentCharacter + " Launch rod longitude: " + simulationLRLon + System.lineSeparator()
            + commentCharacter + " Launch rod latitude: " + simulationLRLat + System.lineSeparator()
            + commentCharacter + " Launch rod altitude: " + simulationLRAlt;
    fw.append(System.lineSeparator());
    fw.append(simulatedConditions);
  }
  private void writeEventsToFOS(FileWriter fw, FlightDataBranch branch) throws IOException{
    List<Double> t = branch.get(FlightDataType.TYPE_TIME);
    int pointID = 0;
    for(FlightEvent fe : branch.getEvents()){
      while(pointID < branch.getLength() && t.get(pointID) < fe.getTime()){
        pointID++;
      }
      fw.append(System.lineSeparator());
      fw.append(commentCharacter + " Event " + fe.getType().name() +
              " occurred at " + String.format("%.2f", fe.getTime()) + " seconds, data point " + pointID);
    }
  }
  private void writeWarningsToFOS(FileWriter fw, Simulation simulation) throws IOException{
    for(Warning w : simulation.getSimulatedWarnings()){
      fw.append(System.lineSeparator());
      fw.append(commentCharacter + " " + w.toString());
    }
  }
  private void writeVariablesToFOS(FileWriter fw, FlightDataType[] fields, Unit[] units) throws IOException{
    for(int i = 0; i < fields.length; i++){
      FlightDataType f = fields[i];
      Unit u = units[i];
      fw.append(System.lineSeparator());
      fw.write(commentCharacter + 
              " " + f.getName() + 
              " (" + u.getUnit() + ")");
    }
  }
  private void writeDataToFOS(FileWriter fw, FlightDataBranch branch, FlightDataType[] fields) throws IOException{
    ArrayList<List<Double>> data = new ArrayList<>();
    
    for (FlightDataType field : fields) {
      data.add(branch.get(field));
    }
    String[] dataPoint = new String[fields.length];
    for(int i = 0; i < branch.getLength(); i++){
      for(int j = 0; j < fields.length; j++){
        if(data.get(j) == null){
          dataPoint[j] = String.format("%.2f", Double.NaN);
        } else{
          dataPoint[j] = String.format("%.2f", data.get(j).get(i));
        }
      }
      fw.append(System.lineSeparator());
      fw.append(String.join(fieldSeparator, dataPoint));
    }
  }
}
