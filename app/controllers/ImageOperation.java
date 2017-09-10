package controllers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import annotation.ControllerHandler;
import play.Play;
import play.mvc.*;
import play.cache.*;

@With(ControllerHandler.class)
public class ImageOperation extends Controller {

	private static BufferedImage loadImage(String path) throws IOException {
		String key = "imgOp-" + path;
		BufferedImage img = (BufferedImage) Cache.get(key);
		if (img == null) {
			img = ImageIO.read(Play.application().resourceAsStream(path));
			Cache.set(key, img);
		}

		return img;
	}

	private static Result imageResult(BufferedImage img, String format) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, format, baos);
		return ok(baos.toByteArray()).as("image/" + format);
	}

	private static BufferedImage tintImage(BufferedImage img, Color tintColor) {
		double tintRatioR = tintColor.getRed() / 255.0;
		double tintRatioG = tintColor.getGreen() / 255.0;
		double tintRatioB = tintColor.getBlue() / 255.0;

		BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {

				Color c = new Color(img.getRGB(x, y), true);

				int r = c.getRed();
				int g = c.getGreen();
				int b = c.getBlue();
				int a = c.getAlpha();

				r *= tintRatioR;
				g *= tintRatioG;
				b *= tintRatioB;

				c = new Color(r, g, b, a);

				newImg.setRGB(x, y, c.getRGB());
			}
		}

		return newImg;
	}

	public static Result tintImage() {
		String basePath = "public/images/";
		String msg = "";
		try {

			int r = 0, g = 0, b = 0;
			String extension = "";
			String path = "";

			final Set<Map.Entry<String, String[]>> entries = request().queryString().entrySet();
			for (Map.Entry<String, String[]> entry : entries) {
				String key = entry.getKey();
				String[] values = entry.getValue();

				if (key.equals("r")) {
					r = Integer.valueOf(values[0]);
				} else if (key.equals("g")) {
					g = Integer.valueOf(values[0]);
				} else if (key.equals("b")) {
					b = Integer.valueOf(values[0]);
				} else if (key.equals("path")) {
					String fileName = values[values.length - 1];
					int index = fileName.lastIndexOf('.');
					extension = fileName.substring(index + 1, fileName.length());

					for (int i = 0; i < values.length; i++) {
						path += values[i];
						if (i < values.length - 1) {
							path += "/";
						}
					}
				}

			}

			BufferedImage img = loadImage(basePath + path);

			img = tintImage(img, new Color(r, g, b));


			return imageResult(img, extension);

		} catch (Exception e) {

			e.printStackTrace();

		}

		return badRequest();

	}

	private static BufferedImage squareCut(BufferedImage img) {
		int size = Math.min(img.getWidth(), img.getHeight());
		BufferedImage sImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

		int dx = img.getWidth() - size;
		int dy = img.getHeight() - size;

		Graphics g = sImg.getGraphics();
		g.drawImage(img, -dx / 2, -dy / 2, null);

		return sImg;
	}

	public static Result squareCut(String imageName) {
		String path = "public/images/" + imageName;
		try {
			int i = path.lastIndexOf('.');
			String extension = path.substring(i + 1, path.length());

			BufferedImage img = loadImage(path);
			img = squareCut(img);
			return imageResult(img, extension);
		} catch (IOException e) {

			e.printStackTrace();
		}

		return badRequest();

	}

	private final static String[] monthNames = { "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" };

	private static List<String> toStrTokens(int val) {
		List<String> list = new ArrayList<String>();
		String valStr = val + "";
		for (int i = 0; i < valStr.length(); i++) {
			list.add(valStr.charAt(i) + "");
		}
		return list;
	}

	private static BufferedImage createCheckedImage(Date date) throws IOException {
		String folder = "public/images/check/";

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY) + 1;
		int minute = calendar.get(Calendar.MINUTE);
		String surfix = "am";

		if (hour == 24) {
			hour = 0;
		} else if (hour == 12) {
			surfix = "pm";
		} else if (hour > 12) {
			surfix = "pm";
			hour -= 12;
		}

		ArrayList<String> tokens = new ArrayList<String>();

		tokens.add(monthNames[month].substring(0, 3).toLowerCase());
		tokens.add(null);
		tokens.addAll(toStrTokens(day));
		tokens.add(null);
		tokens.addAll(toStrTokens(hour));
		tokens.add("colon");
		tokens.addAll(toStrTokens(minute));
		tokens.add(surfix);

		ArrayList<BufferedImage> tokenImgs = new ArrayList<BufferedImage>();
		for (String t : tokens) {
			if (t == null) {
				tokenImgs.add(null);
			} else {
				tokenImgs.add(loadImage(folder + t + ".png"));
			}
		}

		double rotation = Math.toRadians(18);

		BufferedImage frame = loadImage(folder + "checked.png");
		int frameWidth = frame.getWidth();
		int frameHeight = frame.getHeight();

		double h1 = Math.sin(rotation) * frameWidth;
		double h2 = Math.cos(rotation) * frameHeight;

		double w1 = Math.sin(rotation) * frameHeight;
		double w2 = Math.cos(rotation) * frameWidth;

		double width = w1 + w2;
		double height = h1 + h2;
		BufferedImage image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) image.getGraphics();

		g.translate(0, h1);
		g.rotate(-rotation);

		g.drawImage(frame, 0, 0, null);

		// positions
		int space = 10;
		int gap = 5;
		int centerX = frameWidth / 2;
		int centerY = frameHeight / 2 + 10;

		int textWidth = 0;
		for (BufferedImage img : tokenImgs) {
			textWidth += img == null ? space : img.getWidth() + gap;
		}

		int x = centerX - textWidth / 2;
		for (BufferedImage img : tokenImgs) {
			if (img != null) {

				g.drawImage(img, x, centerY, null);
			}
			x += img == null ? space : img.getWidth() + gap;
		}

		return image;

	}

	public static Result checkedImage() {

		try {

			Date d = new Date(request().getQueryString("date"));
			BufferedImage img = createCheckedImage(d);

			return imageResult(img, "png");
		} catch (IOException e) {

			e.printStackTrace();
		}

		return badRequest();

	}
}
