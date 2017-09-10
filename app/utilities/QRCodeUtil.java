package utilities;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;
import java.util.Random;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ning.http.util.Base64;

import play.Logger;
import play.api.cache.Cache;

/**
 * 二维码工具类
 * 
 */
public class QRCodeUtil {

	private static final String CHARSET = "utf-8";
	private static final String FORMAT_NAME = "JPG";
	// 二维码尺寸
	private static final int QRCODE_SIZE = 300;
	// LOGO宽度
	public static final int WIDTH = 60;
	// LOGO高度
	public static final int HEIGHT = 60;

	private static BufferedImage createImage(String content, Image icon) throws Exception {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
				BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000
						: 0xFFFFFFFF);
			}
		}
		if (icon == null) {
			return image;
		}
		// 插入图片
		QRCodeUtil.insertImage(image, icon);
		return image;
	}

	private static void insertImage(BufferedImage source, Image icon) throws Exception {


		// 插入LOGO
		Graphics2D graph = source.createGraphics();
		int width = icon.getWidth(null);
		int height = icon.getHeight(null);
		int x = (QRCODE_SIZE - width) / 2;
		int y = (QRCODE_SIZE - height) / 2;

		graph.drawImage(icon, x, y, width, height, null);
		Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}

	public static Image GetIconImage(String resource, boolean compress) {

		String key = "imagecache." + resource +"." + compress;
		Image cached = (Image)play.cache.Cache.get(key);
		if(cached != null){
			return cached;
		}
		Image src = null;
		try {
			InputStream logoStream = play.Play.application().resourceAsStream(resource);
			src = ImageIO.read(logoStream);
		} catch (IOException e) {
			Logger.error("Failed to get icon file", e);
			return null;
		}
		int width = src.getWidth(null);
		int height = src.getHeight(null);

		if(compress) {
			if (width > WIDTH) {
				width = WIDTH;
			}
			if (height > HEIGHT) {
				height = HEIGHT;
			}
			Image image = src.getScaledInstance(width, height,
					Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			src = image;
		}

		play.cache.Cache.set(key, src);
		return src;

	}

	public static String encodeBase64(String content, Image icon) {
		try {
			BufferedImage image = QRCodeUtil.createImage(content, icon);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(image, "JPG", out);
			String base64bytes = Base64.encode(out.toByteArray());
			return "data:image/jpg;base64," + base64bytes;
		} catch (Exception e) {
			Logger.error("Failed to generate QRCode.", e);
			return null;
		}
	}

	public static byte[] encodeImage(String content, Image icon) {
		try {
			BufferedImage image = QRCodeUtil.createImage(content, icon);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(image, "JPG", out);
			return out.toByteArray();
		} catch (Exception e) {
			Logger.error("Failed to generate QRCode.", e);
			return null;
		}
	}

	/**
	 * 解析二维码
	 *
	 * @param file 二维码图片
	 * @return
	 * @throws Exception
	 */
	/*public static String decode(File file) throws Exception {
		BufferedImage image;
		image = ImageIO.read(file);
		if (image == null) {
			return null;
		}
		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(
				image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result;
		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
		hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
		result = new MultiFormatReader().decode(bitmap, hints);
		String resultStr = result.getText();
		return resultStr;
	}*/
}
// insert the class
class BufferedImageLuminanceSource extends LuminanceSource {
	private final BufferedImage image;
	private final int left;
	private final int top;

	public BufferedImageLuminanceSource(BufferedImage image) {
		this(image, 0, 0, image.getWidth(), image.getHeight());
	}

	public BufferedImageLuminanceSource(BufferedImage image, int left, int top,
			int width, int height) {
		super(width, height);

		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();
		if (left + width > sourceWidth || top + height > sourceHeight) {
			throw new IllegalArgumentException(
					"Crop rectangle does not fit within image data.");
		}

		for (int y = top; y < top + height; y++) {
			for (int x = left; x < left + width; x++) {
				if ((image.getRGB(x, y) & 0xFF000000) == 0) {
					image.setRGB(x, y, 0xFFFFFFFF); // = white
				}
			}
		}

		this.image = new BufferedImage(sourceWidth, sourceHeight,
				BufferedImage.TYPE_BYTE_GRAY);
		this.image.getGraphics().drawImage(image, 0, 0, null);
		this.left = left;
		this.top = top;
	}

	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		image.getRaster().getDataElements(left, top + y, width, 1, row);
		return row;
	}

	public byte[] getMatrix() {
		int width = getWidth();
		int height = getHeight();
		int area = width * height;
		byte[] matrix = new byte[area];
		image.getRaster().getDataElements(left, top, width, height, matrix);
		return matrix;
	}

	public boolean isCropSupported() {
		return true;
	}

	public LuminanceSource crop(int left, int top, int width, int height) {
		return new BufferedImageLuminanceSource(image, this.left + left,
				this.top + top, width, height);
	}

	public boolean isRotateSupported() {
		return true;
	}

	public LuminanceSource rotateCounterClockwise() {
		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();
		AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0, 0.0,
				0.0, sourceWidth);
		BufferedImage rotatedImage = new BufferedImage(sourceHeight,
				sourceWidth, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = rotatedImage.createGraphics();
		g.drawImage(image, transform, null);
		g.dispose();
		int width = getWidth();
		return new BufferedImageLuminanceSource(rotatedImage, top, sourceWidth
				- (left + width), getHeight(), width);
	}
}
