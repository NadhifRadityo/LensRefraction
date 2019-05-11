package io.github.NadhifRadityo.LensRefraction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;

import javax.swing.JFrame;
import javax.swing.UIManager;

import io.github.NadhifRadityo.Objects.Canvas.CanvasPanel;
import io.github.NadhifRadityo.Objects.Canvas.Sprite;
import io.github.NadhifRadityo.Objects.Canvas.Managers.FrameLooperManager;
import io.github.NadhifRadityo.Objects.Canvas.Managers.FrameLooperManager.FrameUpdater;
import io.github.NadhifRadityo.Objects.Canvas.Managers.GraphicModifierManager;
import io.github.NadhifRadityo.Objects.Canvas.Managers.GraphicModifierManager.CustomGraphicModifier;
import io.github.NadhifRadityo.Objects.Canvas.Managers.KeyListenerManager;
import io.github.NadhifRadityo.Objects.Canvas.Managers.KeyListenerManager.CustomKeyListener;
import io.github.NadhifRadityo.Objects.Canvas.Managers.MouseListenerManager;
import io.github.NadhifRadityo.Objects.Canvas.Managers.MouseListenerManager.CustomMouseListener;
import io.github.NadhifRadityo.Objects.Canvas.RenderHints.AntiAlias;
import io.github.NadhifRadityo.Objects.Canvas.RenderHints.FillSprite;
import io.github.NadhifRadityo.Objects.Canvas.RenderHints.FontChanger;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Lens;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Line;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Point;
import io.github.NadhifRadityo.Objects.Canvas.Shapes.Text;
import io.github.NadhifRadityo.Objects.Thread.Handler;
import io.github.NadhifRadityo.Objects.Thread.HandlerThread;
import io.github.NadhifRadityo.Objects.Utilizations.DimensionUtils;
import io.github.NadhifRadityo.Objects.Utilizations.FlatColor;
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
		
		GraphicModifierManager graphicManager = new GraphicModifierManager(true, -2);
		graphicManager.addModifier(new AntiAlias(true));
		graphicManager.addModifier(new FontChanger(new Font("Segoe UI", Font.PLAIN, 20)));
		graphicManager.addModifier(new CustomGraphicModifier() {
			@Override public void draw(Graphics2D g) {
				g.setColor(FlatColor.Midnight_Blue.getColor());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(FlatColor.Clouds.getColor());
			} @Override public void reset(Graphics2D g) { }
		}, -2);
		canvasPanel.addManager(graphicManager);
		drawRefraction();
	}

	Color lensColor = FlatColor.Silver.getColor(0.7f);
	java.awt.Point lensStartPos;
	int lensOriginX; int lensOriginY;
	
	boolean insertFpsValue = false;
	boolean insertLagValue = false;
	long lagValue = 0;
	
	public void drawRefraction() throws Exception {
		HandlerThread handlerThread = new HandlerThread("Animate Stuffs");
		handlerThread.start();
		handlerThread.getLooper().setExceptionHandler(e -> e.printStackTrace());
		Handler handler = new Handler(handlerThread.getLooper());
		
		Text fpsText = new Text(0, 0, "FPS: 0");
		// Even with local sprite, the frame looper will target the first parent, thus it will repaint all.
		// @see Method#init (FrameLooperManager.java#75 - #81)
		FrameLooperManager frameLooper = new FrameLooperManager(false, true, handler, 5);
		frameLooper.setFps(120);
		frameLooper.addSprite(fpsText);
		frameLooper.addUpdater(new FrameUpdater() {
			long delta; long lastTime; int frameCount;
			@Override public void update() {
				if(lagValue > 0) try { Thread.sleep(lagValue); } catch (InterruptedException e) { e.printStackTrace(); }
				fpsText.setPosition(getWidth() - fpsText.getWidth() - 17, 0);
				long current = System.currentTimeMillis();
				delta += current - lastTime; lastTime = current;
				frameCount++; if(delta < 1000) return;
				delta = delta % 1000;
				fpsText.setText("FPS: " + frameCount); frameCount = 0;
				fpsText.setPosition(getWidth() - fpsText.getWidth() - 17, 0);
			}
		});
		frameLooper.setRunBehindCallback(delay -> { if(delay > 10) System.out.println("Late: " + delay + "ms"); });
		canvasPanel.addManager(frameLooper);
		
		Lens lens = new Lens(200, 100 / 2, 80, 200);
		Text lensText = new Text(0, 0, "Focal Point: 0");
		GraphicModifierManager lensGraphicManager = new GraphicModifierManager(false, 1);
		lensGraphicManager.addSprite(lens);
		lensGraphicManager.addSprite(lensText);
		lensGraphicManager.addModifier(new FillSprite(true));
		lensGraphicManager.addModifier(new CustomGraphicModifier() {
			@Override public void draw(Graphics2D g) { g.setColor(lensColor); }
			@Override public void reset(Graphics2D g) { g.setColor(FlatColor.Clouds.getColor()); }
		}, 1);
		lensGraphicManager.addModifier(new CustomGraphicModifier() {
			@Override public void draw(Graphics2D g) { lensText.setPosition(lens.getX() + lens.getWidth() / 2 - lensText.getWidth() / 2, lens.getY() + lens.getHeight() + 10); }
			@Override public void reset(Graphics2D g) { }
		});
		lensGraphicManager.addModifier(new FontChanger(new Font("Segoe UI", Font.PLAIN, 14)));
		canvasPanel.addManager(lensGraphicManager);
		
		GraphicModifierManager lightGraphicManager = new GraphicModifierManager(false, 0);
		lightGraphicManager.addSprite(new Sprite(0, 0) {
//			int focalPointX = 0; int increment = 1;
			int lightSources = 17;
			long backforthTime = 5000, lastTime = System.currentTimeMillis(),
				elapsed = 0;
			boolean back = false;
			@Override public void draw(Graphics g) {
				Sprite[] lightSprites = lightGraphicManager.getSprites().keySet().toArray(new Sprite[0]);
				for(Sprite sprite : lightSprites) {
					if(sprite.equals(this)) continue;
					lightGraphicManager.removeSprite(sprite);
				}
				
				long current = System.currentTimeMillis();
				elapsed += (back ? -1 : 1) * (current - lastTime);
				lastTime = current;
				if(elapsed > backforthTime / 2) back = true;
				if(elapsed <= 0) back = false;
				
				Point focalPoint = new Point((int) NumberUtils.map(elapsed, 0, backforthTime / 2, 0, getWidth()), lens.getY() + lens.getHeight() / 2);
				focalPoint.setPosition(Math.max(0, Math.min(getWidth(), focalPoint.getX())), focalPoint.getY());
				lensText.setText("Focal Point: " + (focalPoint.getX() - (lens.getX() + lens.getWidth() / 2)));
				lightGraphicManager.addSprite(focalPoint);
				for(int i = 0; i < lightSources; i++) {
					int lineY = NumberUtils.map(i, 0, lightSources - 1, lens.getY(), lens.getY() + lens.getHeight());
					Point startPoint = new Point(0, lineY);
					Point endPoint = new Point(lens.getX() + lens.getWidth() / 2, lineY);
					lightGraphicManager.addSprite(new Line(startPoint, endPoint));
					lightGraphicManager.addSprite(startPoint);
					lightGraphicManager.addSprite(endPoint);
					lightGraphicManager.addSprite(new Line(endPoint, focalPoint).extend(500));
				}
//				focalPointX += increment; focalPointX = Math.min(getWidth(), focalPointX);
//				if(focalPointX >= getWidth() || focalPointX <= 0) increment *= -1;
			} @Override public Area getArea() { return null; }
			@Override public boolean equals(Object other) { return this == other; }
		});
		canvasPanel.addManager(lightGraphicManager);
		
		MouseListenerManager mouseManager = new MouseListenerManager(true);
		mouseManager.addListener(new CustomMouseListener(lens) {
			@Override public void mouseEntered(MouseEvent e) {
				lensColor = FlatColor.Alizarin.getColor(0.7f);
			}
			@Override public void mouseExited(MouseEvent e) {
				lensColor = FlatColor.Silver.getColor(0.7f);
			}
			@Override public void mouseDragged(MouseEvent e) {
				lensColor = FlatColor.Pomegranate.getColor(0.7f);
				if(lensStartPos == null) return;
				int distanceX = (int) (e.getX() - lensStartPos.getX());
				int distanceY = (int) (e.getY() - lensStartPos.getY());
				lens.setPosition(distanceX + lensOriginX, distanceY + lensOriginY);
			}
			@Override public void mousePressed(MouseEvent e) {
				lensStartPos = e.getPoint(); lensOriginX = lens.getX(); lensOriginY = lens.getY();
				lensColor = FlatColor.Pomegranate.getColor(0.7f);
			};
			@Override public void mouseReleased(MouseEvent e) {
				lensStartPos = null; lensOriginX = 0; lensOriginY = 0;
				lensColor = FlatColor.Alizarin.getColor(0.7f);
			};
		});
		mouseManager.addListener(new CustomMouseListener(null) {
			@Override public void mouseClicked(MouseEvent e) {
				canvasPanel.requestFocus();
			};
			@Override public void mouseDragged(MouseEvent e) {
				if(lensStartPos == null) return;
				int distanceX = (int) (e.getX() - lensStartPos.getX());
				int distanceY = (int) (e.getY() - lensStartPos.getY());
				lens.setPosition(distanceX + lensOriginX, distanceY + lensOriginY);
			};
			@Override public void mouseReleased(MouseEvent e) {
				lensStartPos = null; lensOriginX = 0; lensOriginY = 0;
			};
		});
		canvasPanel.addManager(mouseManager);
		
		KeyListenerManager keyManager = new KeyListenerManager(true);
		keyManager.addListener(new CustomKeyListener(null, KeyEvent.VK_L) {
			public void keyPressed(KeyEvent e) { System.out.println("Insert Lag"); insertLagValue = true; lensColor = FlatColor.Emerald.getColor(0.7f); };
		});
		keyManager.addListener(new CustomKeyListener(null, KeyEvent.VK_F) {
			public void keyPressed(KeyEvent e) { System.out.println("Insert Fps"); insertFpsValue = true; lensColor = FlatColor.Emerald.getColor(0.7f); };
		});
		keyManager.addListener(new CustomKeyListener(null) {
			String inputNumber = "";
			public void keyPressed(KeyEvent e) {
				if(!insertLagValue && !insertFpsValue) { inputNumber = ""; return; }
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
					lensColor = FlatColor.Silver.getColor(0.7f);
					if(insertLagValue) { insertLagValue = false;
						lagValue = (e.getKeyCode() == KeyEvent.VK_ESCAPE) ? lagValue : Long.parseLong(inputNumber);
					} if(insertFpsValue) { insertFpsValue = false;
						frameLooper.setFps((e.getKeyCode() == KeyEvent.VK_ESCAPE) ? frameLooper.getFps() : Integer.parseInt(inputNumber));
					} inputNumber = ""; return;
				} if(!NumberUtils.isNumber(e.getKeyChar() + "")) return;
				inputNumber += e.getKeyChar();
				System.out.println(inputNumber);
			};
		});
		canvasPanel.addManager(keyManager);
	}
	
	public static void main(String... strings) {
		try {
			LensRefraction lensRefraction = new LensRefraction();
			lensRefraction.setVisible(true);
		} catch (Exception e) { e.printStackTrace(); }
	}
}
