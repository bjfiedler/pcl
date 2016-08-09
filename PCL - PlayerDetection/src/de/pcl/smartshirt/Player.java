package de.pcl.smartshirt;

import org.opencv.core.Point;

public class Player {
	private int id;
	private String team;
	private Point position;
	private double direction;
	
	public Player(int id, String team, Point position, double direction) {
		this.id = id;
		this.team = team;
		this.position = position;
		this.direction = direction;
	}
	
	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public int getId() {
		return id;
	}

	public String getTeam() {
		return team;
	}
}
