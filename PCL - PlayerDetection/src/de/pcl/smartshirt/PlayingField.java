package de.pcl.smartshirt;

import java.util.ArrayList;

public class PlayingField {

	private static double DIRECTION_LEFT = 0.0;
	private static double DIRECTION_UP_LEFT = 0.75;
	private static double DIRECTION_UP = 1.5;
	private static double DIRECTION_UP_RIGHT = 2.25;
	private static double DIRECTION_RIGHT = -3.00; 
	private static double DIRECTION_DOWN_RIGHT = -2.25;
	private static double DIRECTION_DOWN = -1.8;
	private static double DIRECTION_DOWN_LEFT = -0.75;
	private static double DIRECTION_TRESHOLD = 0.2;
	
	public static final int NOT_ATTACKED = -1;
	public static final int ATTACK_DIRECTION_BEHIND = 0;
	public static final int ATTACK_DIRECTION_LEFT = 1;
	public static final int ATTACK_DIRECTION_RIGHT = 2;
	
	private ArrayList<Player> blueTeam;
	private ArrayList<Player> greenTeam;
	
	public int isPlayerAttacked(Player blue, Player green) {
		// TODO if player are looking at each other return false
		if ((isLeft(blue.getDirection()) && isRight(green.getDirection())) || 
				(isLeft(green.getDirection()) && isRight(blue.getDirection())) ||
				(isUp(blue.getDirection()) && isDown(green.getDirection())) ||
				(isUp(green.getDirection()) && isDown(blue.getDirection())) 				
				) {
			return NOT_ATTACKED;
		}
		
		
		// Check direction in combination with position
		
		if (isLeft(blue.getDirection())) {
			// <-blue   <-green
			// blue.x < green.x
			if (blue.getPosition().x < green.getPosition().x) {
				return ATTACK_DIRECTION_BEHIND;
			} 
			
		} else if (isUp(blue.getDirection())) {
			// blue
			// green
			if (blue.getPosition().y < green.getPosition().y) {
				return ATTACK_DIRECTION_BEHIND;
			}
		} else if (isRight(blue.getDirection())) {
			// green-> blue->
			// blue.x < green.x
			if (blue.getPosition().x > green.getPosition().x) {
				return ATTACK_DIRECTION_BEHIND;
			}
		} else if (isDown(blue.getDirection())) {
			// green
			// blue
			if (blue.getPosition().y > green.getPosition().y) {
				return ATTACK_DIRECTION_BEHIND;
			}
		}
		
		return NOT_ATTACKED;
	}
	
	
	
	private boolean isLeft(double direction) {
		if ((direction >= -0.75) && (direction < 0.75)) {
			return true;
		}
		return false;
	}
	
	private boolean isUp(double direction) {
		if (direction >= 0.75 && direction < 2.25) {
			return true;
		}
		return false;
	}
	
	private boolean isRight(double direction) {
		if ((direction >= 2.25 && direction <= 3.00)
				|| (direction <= -3.0 && direction < -2.25)) {
			return true;
		}
		
		return false;
	}
	
	private boolean isDown(double direction) {
		if (direction >= -2.25 && direction < -0.75) {
			return true;
		}
		return false;
	}
	
	
	
	
}
