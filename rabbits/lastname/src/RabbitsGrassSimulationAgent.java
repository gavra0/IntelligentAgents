import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.*;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    // position of the rabbit
    private int x;
    private int y;
    // the next step directions
    private int changeX;
    private int changeY;

    // how much energy rabbit currently has
    private int energy;

    private int id;
    public static int TOTAL_ID = 0;

    private RabbitsGrassSimulationSpace rabbitsGrassSimulationSpace;

    public RabbitsGrassSimulationAgent(int x, int y, int energy) {
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.id = TOTAL_ID++;
    }

    /**
     * Create a new baby rabbit if the energy of the parent is greater than the threshold
     *
     * @param threshold   min amount of energy in order to reproduce
     * @return new baby rabbit if the reproduction is success, or {@code NULL} if not
     */
    public RabbitsGrassSimulationAgent reproduce(int threshold) {
        if (energy >= threshold) {
            RabbitsGrassSimulationAgent babyRabbit = new RabbitsGrassSimulationAgent(-1, -1, (int) Math.floor((double) energy/2));
            babyRabbit.rabbitsGrassSimulationSpace = this.rabbitsGrassSimulationSpace;
            if (rabbitsGrassSimulationSpace.addRabbit(babyRabbit)){
                // reduce parents energy
                energy = (int) Math.ceil((double) energy / 2);
                return babyRabbit;
            }
            else{
                return null;
            }
        } else {
            // there is not enough energy for reproduction
            return null;
        }
    }

    /**
     * Set next move for the rabbit
     */
    private void chooseNextDirection() {
        int combinationNum = (int) Math.floor(Math.random() * 4);
        changeX = changeY = 0;
        switch (combinationNum){
            case 0: changeY = -1; break;
            case 1: changeY = 1; break;
            case 2: changeX = -1; break;
            case 3: changeX = 1;
        }
    }

    /**
     * Try to move rabbit to a new cell, if the cell is occupied just skip movement for the cycle
     */
    public void step() {
        chooseNextDirection();
        int newX = x + changeX;
        int newY = y + changeY;

        Object2DGrid grid = rabbitsGrassSimulationSpace.getRabbitSpace();
        newX = newX % grid.getSizeX();
        newY = newY % grid.getSizeY();

        // try to move the rabbit
        rabbitsGrassSimulationSpace.moveAgentAt(x, y, newX, newY);
        // eat some grass if there is some in the cell (this might be new cell, or the previous one if there was no move
        energy += rabbitsGrassSimulationSpace.powerUp(x, y);
        // step energy reduce
        energy--;
    }

    /**
     * Draws the rabbit on the grid
     *
     * @param simGraphics
     */
    @Override
    public void draw(SimGraphics simGraphics) {
        if (energy > 10) {
            simGraphics.drawFastRoundRect(new Color(255, 20, 40, 255));
        } else {
            simGraphics.drawFastRoundRect(new Color(255, 251, 55, 255));
        }
    }

    /**
     * GETTERS AND SETTERS BELOW *
     */
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getChangeX() {
        return changeX;
    }

    public void setChangeX(int changeX) {
        this.changeX = changeX;
    }

    public int getChangeY() {
        return changeY;
    }

    public void setChangeY(int changeY) {
        this.changeY = changeY;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RabbitsGrassSimulationSpace getRabbitGrassSpace() {
        return rabbitsGrassSimulationSpace;
    }

    public void setRabbitGrassSpace(RabbitsGrassSimulationSpace rabbitsGrassSimulationSpace) {
        this.rabbitsGrassSimulationSpace = rabbitsGrassSimulationSpace;
    }

}
