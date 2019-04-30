package io.github.NadhifRadityo.LensRefraction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.UIManager;

import io.github.NadhifRadityo.Objects.Canvas.CanvasPanel;
import io.github.NadhifRadityo.Objects.Canvas.CustomGraphicModifier;
import io.github.NadhifRadityo.Objects.Canvas.RenderHints.AntiAlias;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Lens;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Line;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Point;
import io.github.NadhifRadityo.Objects.Utilizations.DimensionUtils;
import io.github.NadhifRadityo.Objects.Utilizations.NumberUtils;

public class LensRefraction extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3196356202749585923L;
	private Dimension windowDim;
	private CanvasPanel canvasPanel;
	
	public LensRefraction() throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		windowDim = new Dimension(766, 500);
		setSize(windowDim);
		setPreferredSize(windowDim);
		setLayout(new GridLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		canvasPanel = new CanvasPanel();
		canvasPanel.setSize(DimensionUtils.getMaxDimension());
		canvasPanel.setPreferredSize(DimensionUtils.getMaxDimension());
		add(canvasPanel);
		
		canvasPanel.addSprite(new AntiAlias(true), -2);
		canvasPanel.addSprite(new CustomGraphicModifier(true) {
			@Override public void draw(Graphics2D g) {
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.WHITE);
			} @Override public void reset(Graphics2D g) { }
		}, -2);
		drawRefraction();
	}
	
	int lightSources = 100;
	public void drawRefraction() {
//		Point object = new Point(100, 100);
//		canvasPanel.addSprite(object);
		
		Lens lens = new Lens(200, 100 / 2, 80, 200);
		canvasPanel.addSprite(lens, -1);
		
		Point focalPoint = new Point(400, lens.getY() + lens.getHeight() / 2);
		canvasPanel.addSprite(focalPoint);
		
		for(int i = 0; i < lightSources; i++) {
			int lineY = NumberUtils.map(i, 0, lightSources, lens.getY(), lens.getY() + lens.getHeight());
			Point startPoint = new Point(0, lineY);
			Point endPoint = new Point(lens.getX() + lens.getWidth() / 2, lineY);
			canvasPanel.addSprite(new Line(startPoint, endPoint));
			canvasPanel.addSprite(startPoint);
			canvasPanel.addSprite(endPoint);
			canvasPanel.addSprite(new Line(endPoint, focalPoint).extend(500));
		}
	}
	
	public static void main(String... strings) {
		try {
			LensRefraction lensRefraction = new LensRefraction();
			lensRefraction.setVisible(true);
		} catch (Exception e) { e.printStackTrace(); }
	}
}
