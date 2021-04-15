package net.toshimichi.invasion;

public class Border {
    private final int phase;
    private final int peace;
    private final double to;
    private final int time;

    public Border(int phase, int peace, double to, int time) {
        this.phase = phase;
        this.peace = peace;
        this.to = to;
        this.time = time;
    }

    public int getPhase() {
        return phase;
    }

    public int getPeace() {
        return peace;
    }

    public double getTo() {
        return to;
    }

    public int getTime() {
        return time;
    }
}
