import com.beust.jcommander.Parameter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class JCommanderInput {
	@Parameter
	private List<String> parameters = new ArrayList<>();

	@Parameter(names = { "--agent1", "-a1"}, description = "First agent's type (random, mcts, ga, ann, neat, cnn)", required = true)
		private String agent1 = "human";
		
	@Parameter(names = { "--agent2", "-a2"}, description = "First agent's type (random, mcts, ga, ann, neat, cnn)", required = true)
		private String agent2 = "human";

	@Parameter(names = { "--size", "-s"}, description = "Size of game board (square) (9-19)")
		private int boardSize = 9;

	@Parameter(names = { "--game", "-g"}, description = "Type of game being played (go, hex, sprouts)")
		private String game = "go";

	@Parameter(names = { "--time", "-t"}, description = "How long agents have to make a move")
		private int timeAllowed = 3;

	@Parameter(names = "--help", description = "Display usage info)")
		private boolean help = false;

	public String getAgent1(){
		return agent1;
	}

	public String getAgent2(){
		return agent2;
	}

	public int getBoardSize(){
		return boardSize;
	}

	public String getGame(){
		return game;
	}

	public boolean getHelp(){
		return help;
	}

	public int getTimeAllowed(){
		return timeAllowed;
	}

	public boolean validateParams(){
		String[] validAgents = {"human","random","mcts","ga","ann","neat","cnn"};
		String[] validGames = {"go", "hex", "sprouts"};
		// Validate agent types
		if(!Arrays.asList(validAgents).contains(this.getAgent1())){
			System.out.println("Agent1 type is invalid");
			return false;
		}
		if(!Arrays.asList(validAgents).contains(this.getAgent2())){
			System.out.println("Agent2 type is invalid");
			return false;
		}

		// Validate board size
		if(this.getBoardSize() < 1 || this.getBoardSize() > 19){
			System.out.println("Invalid board size");
			return false;
		}

		// Validate game type
		if(!Arrays.asList(validGames).contains(this.getGame())){
			System.out.println("Invalid game type");
			return false;
		}

		return true;
	}

}
