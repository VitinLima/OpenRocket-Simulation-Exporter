/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crt.extensions.Exporter;

import javax.swing.JComponent;
import javax.swing.JPanel;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;

/**
 *
 * @author 160047412
 */
@Plugin
public class SimulationExporterConfigurator extends AbstractSwingSimulationExtensionConfigurator<SimulationExporterExtension>{

    public SimulationExporterConfigurator() {
        super(SimulationExporterExtension.class);
    }
    
    @Override
    protected JComponent getConfigurationComponent(SimulationExporterExtension e, Simulation simulation, JPanel panel) {
        panel.add(new SimulationExporterPanel(e));
        return panel;
    }
    
}
