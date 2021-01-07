package players.ai;

import java.io.*;

import players.Player;

public abstract class AI_Player extends Player {
	public abstract void train() throws IOException, ClassNotFoundException;
}
