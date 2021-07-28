package de.uniks.stp.minigame;

public class GameScore {
    public int maxRounds = 5;
    private int ownScore = 0;
    private int opponentScore = 0;

    public int getOwnScore() {
        return ownScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public GameScore increaseOwnScore() {
        ownScore++;
        return this;
    }

    public GameScore increaseOpponentScore() {
        opponentScore++;
        return this;
    }

    public boolean isOwnWin() {
        return (maxRounds - ownScore) < (maxRounds / 2.0);
    }

    public boolean isOwnLoss() {
        return (maxRounds - opponentScore) < (maxRounds / 2.0);
    }

    public void recycle() {
        ownScore = 0;
        opponentScore = 0;
    }

    @Override
    public String toString() {
        return ownScore + " : " + opponentScore;
    }
}
