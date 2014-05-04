import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author Perovic Gavrilovic
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {

    // World size
    private static final int WORLD_X_SIZE = 20;
    private static final int WORLD_Y_SIZE = 20;

    // Population constants
    private static final int NUM_RABBITS = 30;
    private static final int INIT_ENERGY = 8;
    private static final int BIRTH_THRESHOLD = 15;
    private static final int GRASS_GROWTH_RATE = 30;

    private int worldXSize = WORLD_X_SIZE;
    private int worldYSize = WORLD_Y_SIZE;
    private int numRabbits = NUM_RABBITS;
    private int birthThreshold = BIRTH_THRESHOLD;
    private int grassGrowthRate = GRASS_GROWTH_RATE;

    private int initEnergy = INIT_ENERGY;

    private List<RabbitsGrassSimulationAgent> rabbits;
    private RabbitsGrassSimulationSpace rabbitsGrassSimulationSpace;

    private Schedule schedule;
    private DisplaySurface displaySurface;

    private OpenSequenceGraph populationGraph;

    public static void main(String[] args) {
        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);
    }

    // get the number of live rabbits
    class RabbitsAlive implements DataSource, Sequence {

        public Object execute() {
            return new Double(getSValue());
        }

        public double getSValue() {
            return (double) rabbits.size();
        }
    }

    // get the total amount of energy in the grass
    class GrassCount implements DataSource, Sequence {
        public Object execute() {
            return new Double(getSValue());
        }

        public double getSValue() {
            return (double) rabbitsGrassSimulationSpace.grassCount();
        }
    }

    /**
     * Have a nice name for our simulation
     *
     * @return
     */
    public String getName() {
        return "Rabbit and grass simulation";
    }

    /**
     * Start the simulation
     */
    public void begin() {
        if (buildModel()){
            buildSchedule();
            buildDisplay();

            displaySurface.display();
            populationGraph.display();
        }
    }

    /**
     * Create space of specified size and add agents
     */
    public boolean buildModel() {
        // this is wrong
        if (0 == worldXSize || 0 == worldYSize){
            System.out.println("Please select valid values for grid size! Rabbits must have at least one cell.");
            stop();
            return false;
        }
        rabbitsGrassSimulationSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);

        // create rabbits
        for (int i = 0; i < numRabbits; i++) {
            addNewAgent();
        }

        // place the rabbits into the space
        if (!rabbitsGrassSimulationSpace.placeRabbits(rabbits)) {
            System.out.println("Some of the rabbits could not be added to the world");
            // must kick em out of the model
            for (int i = 0; i < rabbits.size(); i++) {
                RabbitsGrassSimulationAgent rabbit = rabbits.get(i);
                if (rabbit.getX() == -1 || rabbit.getY() == -1 || null == rabbit.getRabbitGrassSpace()) {
                    rabbits.remove(i);
                    i--;
                }
            }
        }
        // all good
        return true;
    }

    /**
     * Create new rabbit and adds it to the list
     */
    private void addNewAgent() {
        RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent(-1, -1, initEnergy);
        rabbits.add(rabbit);
    }

    public void buildSchedule() {
        // rabbit and grass cycle
        class RabbitGrassStep extends BasicAction {
            public void execute() {
                // lets grow some grass, produce O2 and be happy
                rabbitsGrassSimulationSpace.plantGrass(grassGrowthRate);

                // have little rabbits eat some of that green joy
                SimUtilities.shuffle(rabbits);
                for (int i = 0; i < rabbits.size(); i++) {
                    RabbitsGrassSimulationAgent rabbit = rabbits.get(i);
                    if (rabbit.getEnergy() <= 0) {
                        // remove the rabbit from the space, they are no good
                        rabbitsGrassSimulationSpace.removeRabbit(rabbit.getX(), rabbit.getY());
                        rabbits.remove(rabbit);
                    } else {
                        rabbit.step();
                        // try to reproduce
                        RabbitsGrassSimulationAgent baby = rabbit.reproduce(birthThreshold);
                        if (null != baby) {
                            // if baby rabbit successfully created and placed in the cell
                            rabbits.add(baby);
                        }
                    }
                }
                // stop simulation of no rabbits
                if (rabbits.size() == 0) {
                    stop();
                }
               /* for (int i = 0; i < rabbits.size(); i++) {
                    System.out.print("Rabbit Id = " + rabbits.get(i).getId() + "; energy = " + rabbits.get(i).getEnergy() + " ");
                }
                System.out.println("\nLive rabbits = " + rabbits.size());*/

                // propagate changes to the display
                displaySurface.updateDisplay();
            }
        }
        schedule.scheduleActionBeginning(0, new RabbitGrassStep());

        // population plotting stuff
        class PopulationUpdate extends BasicAction {
            public void execute() {
                // using this invoke the execute methods of the attached data sources which will update their values
                populationGraph.step();
            }
        }
        schedule.scheduleActionAtInterval(5, new PopulationUpdate());
    }

    /**
     * Set colors for different values of grass energy, add rabbits&grass to display, add population plot
     */
    public void buildDisplay() {
        // set colors
        ColorMap map = new ColorMap();

        map.mapColor(0, Color.white);
        for (int i = 1; i < 1000; i++) {
            if (i <= 10) {
                map.mapColor(i, new Color(0, 255, 0, 55 + (int) ((200 * i) / (double) 10)));
            } else {
                map.mapColor(i, Color.green);
            }
        }
        Value2DDisplay displayGrass =
                new Value2DDisplay(rabbitsGrassSimulationSpace.getGrassSpace(), map);

        Object2DDisplay displayAgents = new Object2DDisplay(rabbitsGrassSimulationSpace.getRabbitSpace());
        displayAgents.setObjectList(rabbits);
        // add grass and rabbits to display
        displaySurface.addDisplayableProbeable(displayGrass, "Grass");
        displaySurface.addDisplayableProbeable(displayAgents, "Rabbits");
        // add data sources to the plot
        populationGraph.addSequence("Rabbits alive", new RabbitsAlive());
        populationGraph.addSequence("Grass count", new GrassCount());
    }

    /**
     * Simulation editable parameters
     *
     * @returns
     */
    public String[] getInitParam() {
        String[] initParams = {"NumRabbits", "WorldXSize", "WorldYSize", "BirthThreshold", "GrassGrowthRate"};
        return initParams;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Initializes the simulation
     */
    public void setup() {
        rabbitsGrassSimulationSpace = null;
        rabbits = new ArrayList<RabbitsGrassSimulationAgent>();
        schedule = new Schedule(1);

        // Tear down Displays
        if (displaySurface != null) {
            displaySurface.dispose();
        }
        displaySurface = null;

        // plots
        if (populationGraph != null) {
            populationGraph.dispose();
        }
        populationGraph = null;

        // create the sliders
        RangePropertyDescriptor pdSizeX = new RangePropertyDescriptor("WorldXSize", 0, 100, 20);
        descriptors.put("WorldXSize", pdSizeX);
        RangePropertyDescriptor pdSizeY = new RangePropertyDescriptor("WorldXSize", 0, 100, 20);
        descriptors.put("WorldYSize", pdSizeY);
        RangePropertyDescriptor numAgents = new RangePropertyDescriptor("NumRabbits", 0, 400, 60);
        descriptors.put("NumRabbits", numAgents);
        RangePropertyDescriptor birthThreshold = new RangePropertyDescriptor("BirthThreshold", 0, 20, 5);
        descriptors.put("BirthThreshold", birthThreshold);
        RangePropertyDescriptor grassGrowthRate = new RangePropertyDescriptor("GrassGrowthRate", 0, 400, 80);
        descriptors.put("GrassGrowthRate", grassGrowthRate);

        // Create Displays
        displaySurface = new DisplaySurface(this, "Carry Drop Model Window 1");
        populationGraph = new OpenSequenceGraph("Population Graph", this);
        populationGraph.setAxisTitles("time", "count");

        // Register Displays
        registerDisplaySurface("Carry Drop Model Window 1", displaySurface);
        this.registerMediaProducer("Plot", populationGraph);
    }


    /**
     * GETTERS AND SETTERS BELOW *
     */
    public int getWorldXSize() {
        return worldXSize;
    }

    public void setWorldXSize(int worldXSize) {
        this.worldXSize = worldXSize;
    }

    public int getWorldYSize() {
        return worldYSize;
    }

    public void setWorldYSize(int worldYSize) {
        this.worldYSize = worldYSize;
    }

    public int getNumRabbits() {
        return numRabbits;
    }

    public void setNumRabbits(int numRabbits) {
        this.numRabbits = numRabbits;
    }

    public int getBirthThreshold() {
        return birthThreshold;
    }

    public void setBirthThreshold(int birthThreshold) {
        this.birthThreshold = birthThreshold;
    }

    public int getGrassGrowthRate() {
        return grassGrowthRate;
    }

    public void setGrassGrowthRate(int grassGrowthRate) {
        this.grassGrowthRate = grassGrowthRate;
    }

    public int getInitEnergy() {
        return initEnergy;
    }

    public void setInitEnergy(int initEnergy) {
        this.initEnergy = initEnergy;
    }

    public List<RabbitsGrassSimulationAgent> getRabbits() {
        return rabbits;
    }

    public void setRabbits(List<RabbitsGrassSimulationAgent> rabbits) {
        this.rabbits = rabbits;
    }

    public RabbitsGrassSimulationSpace getRabbitsGrassSimulationSpace() {
        return rabbitsGrassSimulationSpace;
    }

    public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rabbitsGrassSimulationSpace) {
        this.rabbitsGrassSimulationSpace = rabbitsGrassSimulationSpace;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public DisplaySurface getDisplaySurface() {
        return displaySurface;
    }

    public void setDisplaySurface(DisplaySurface displaySurface) {
        this.displaySurface = displaySurface;
    }
}
