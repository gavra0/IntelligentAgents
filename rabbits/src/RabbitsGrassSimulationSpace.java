import uchicago.src.sim.space.Object2DGrid;

import java.util.List;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 *
 * @author Perovic Gavrilovic
 */

public class RabbitsGrassSimulationSpace {
    private Object2DGrid grassSpace;
    private Object2DGrid rabbitSpace;

    public RabbitsGrassSimulationSpace(int width, int height) {
        grassSpace = new Object2DGrid(width, height);
        // init grass space empty
        for (int i = 0; i < grassSpace.getSizeX(); i++) {
            for (int j = 0; j < grassSpace.getSizeY(); j++) {
                grassSpace.putObjectAt(i, j, new Integer(0));
            }
        }
        // place around energy that is 20% of number of all cells
        plantGrass((int) (width * height * 0.2));

        rabbitSpace = new Object2DGrid(width, height);
    }

    /**
     * Randomly plants specified number of grass
     *
     * @param newPlants number of grass to be plant
     */
    public void plantGrass(int newPlants) {
        for (int i = 0; i < newPlants; i++) {
            // Choose coordinates
            int x = (int) (Math.random() * (grassSpace.getSizeX()));
            int y = (int) (Math.random() * (grassSpace.getSizeY()));

            int currentValue = ((Integer) grassSpace.getObjectAt(x, y)).intValue();
            grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
        }
    }

    /**
     * Returns the amount of the energy available in the grass residing on the cell specified and consumes it
     *
     * @param x position
     * @param y position
     * @return amount of energy in a cell
     */
    public int powerUp(int x, int y) {
        if (!isValidPosition(x, y)) {
            return 0;
        }
        Integer energy = (Integer) grassSpace.getObjectAt(x, y);
        grassSpace.putObjectAt(x, y, new Integer(0));
        return energy.intValue();
    }

    /**
     * Try to move the rabbit to the new cell
     *
     * @param oldX current position on x axis
     * @param oldY current position on y axis
     * @param newX possible new position on the x axis
     * @param newY possible new position on the y axis
     * @return {@code TRUE} if the new position is not occupied, else otherwise
     */
    public boolean moveAgentAt(int oldX, int oldY, int newX, int newY) {
        if (!isValidPosition(newX, newY)) {
            return false;
        }
        if (!isCellOccupied(newX, newY)) {
            RabbitsGrassSimulationAgent rabbitAgent = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(oldX, oldY);
            removeRabbit(oldX, oldY);
            rabbitAgent.setXY(newX, newY);
            rabbitSpace.putObjectAt(newX, newY, rabbitAgent);
            return true;
        } else {
            return false;
        }
    }

    public void removeRabbit(int x, int y) {
        rabbitSpace.putObjectAt(x, y, null);
    }

    private boolean isCellOccupied(int x, int y) {
        return null != rabbitSpace.getObjectAt(x, y);
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && y >= 0 && x < rabbitSpace.getSizeX() && y < rabbitSpace.getSizeY();
    }

    /**
     * Places the rabbits randomly on the grid
     *
     * @param rabbits {@code List<RabbitsGrassSimulationAgent} containing the rabbits
     */
    public boolean placeRabbits(List<RabbitsGrassSimulationAgent> rabbits) {
        boolean successfullyAdded = true;
        for (RabbitsGrassSimulationAgent rabbit : rabbits) {
            successfullyAdded &= addRabbit(rabbit);
        }
        return successfullyAdded;
    }

    /**
     * Try to randomly place a rabbit on a cell
     *
     * @param rabbit rabbit to be placed
     * @return is the placing successfully done
     */
    public boolean addRabbit(RabbitsGrassSimulationAgent rabbit) {
        boolean retVal = false;
        int count = 0;
        int countLimit = 10 * rabbitSpace.getSizeX() * rabbitSpace.getSizeY();

        while ((retVal == false) && (count < countLimit)) {
            int x = (int) (Math.random() * (rabbitSpace.getSizeX()));
            int y = (int) (Math.random() * (rabbitSpace.getSizeY()));
            if (!isCellOccupied(x, y)) {
                rabbitSpace.putObjectAt(x, y, rabbit);
                rabbit.setXY(x, y);
                rabbit.setRabbitGrassSpace(this);
                retVal = true;
            }
            count++;
        }

        return retVal;
    }

    /**
     * Get the number of the grasses
     * @return total count of energy within the grass
     */
    public int grassCount(){
        int totalEnergy = 0;
        for (int i = 0; i < grassSpace.getSizeX(); i++) {
            for (int j = 0; j < grassSpace.getSizeY(); j++) {
                totalEnergy += ((Integer) grassSpace.getObjectAt(i, j)).intValue();
            }
        }
        return totalEnergy;
    }

    /**
     * GETTERS AND SETTERS BELOW *
     */
    public Object2DGrid getGrassSpace() {
        return grassSpace;
    }

    public void setGrassSpace(Object2DGrid grassSpace) {
        this.grassSpace = grassSpace;
    }

    public Object2DGrid getRabbitSpace() {
        return rabbitSpace;
    }

    public void setRabbitSpace(Object2DGrid rabbitSpace) {
        this.rabbitSpace = rabbitSpace;
    }
}
