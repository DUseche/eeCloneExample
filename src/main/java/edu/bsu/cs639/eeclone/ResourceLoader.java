package edu.bsu.cs639.eeclone;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.bsu.cs639.eeclone.audio.OggInputStream;
import edu.bsu.cs639.eeclone.audio.Sound;

/**
 * Loads images.
 * The image naming scheme and resource locations are specified by an xml
 * file, referred to as the <i>index</i> file. 
 * 
 * <p>
 * This class is a singleton: see {@link #instance()}.
 * 
 * @author pvg
 */
public class ResourceLoader {
	
	/** 
	 * Maps key names to the file names.
	 * This is initialized in the static initializer.
	 */
	private static final Map<String,String> keyMap =
		new TreeMap<String,String>();
	

  /**
   * Get the classloader that loaded this class. This classloader will have the
   * correct classpath to resolve image loading, regardless of whether we are
   * running from a flat filesystem or from a jar.
   */
  private static ClassLoader cl = ResourceLoader.class.getClassLoader();
  
	static {
    String indexFile = "resource_index.xml";
    InputStream in = cl.getResourceAsStream(indexFile);
    assert in!=null : "Cannot find resource index file: " + indexFile;
    init(in);
    /*
		try {
			// Read the properties from the file
			Properties p = new Properties();
			InputStream in = cl.getResourceAsStream(Constants.RESOURCE_INDEX);
			assert in != null : 
				"Cannot get resource index: " + Constants.RESOURCE_INDEX;
			p.load(in);
			
			// Fill the name map
			for (Object keyObj : p.keySet()) {
				String key = (String)keyObj;
			  keyMap.put(key, p.getProperty(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    */
	}

  private static final ResourceLoader SINGLETON = new ResourceLoader();

  
  /**
   * Maps image names to their loaded images.
   */
  private Map<String,BufferedImage> imageMap = new HashMap<String,BufferedImage>();

  private Map<String,Sound> soundMap = new HashMap<String,Sound>();
  
  /**
   * Get an instance of the image loader.
   * 
   * @return the singleton image loader
   */
  public static ResourceLoader instance() {
    return SINGLETON;
  }

  /** The configuration for the graphics device used to render images */
  // TODO: determine if this works for fsem vs windowed apps, since fsem can
  // modify the graphics configuration.
  private final GraphicsConfiguration gc = GraphicsEnvironment
      .getLocalGraphicsEnvironment().getDefaultScreenDevice()
      .getDefaultConfiguration();

  /**
   * Default constructor. Private constructor enforces the singleton pattern.
   */
  private ResourceLoader() {
  }
  
  /**
   * Get an image from this image loader.
   * If it has been loaded previously and is cached, the cached copy
   * is returned.  If not, the image will be loaded, initialized, 
   * cached, and returned.
   * 
   * @param name the name of the image, according to the resource index.
   * @return the loaded image
   */
  public BufferedImage getImage(final String name) {
    // There is no synchronization here.  Hence, there is a potential for
    // two threads to load the same image at once, but that's no great loss.
    BufferedImage img = imageMap.get(name);
    if (img==null) {
      img = loadImage(name);
      if (img!=null) imageMap.put(name,img);
      // If it wasn't loaded, then load will have reported the error.
    }
    return img;
  }
  
  /**
   * Get the stream for streaming ogg audio.
   * @param name the resource key
   * @return audio stream
   */
  public OggInputStream getOggStream(final String name)   {
    assert keyMap.containsKey(name);
    InputStream in = cl.getResourceAsStream(keyMap.get(name));
    assert in!=null;
    try {
      return new OggInputStream(in);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Get a sound effect.  Use only for small samples that will fit in memory.
   * @param name
   * @return a sound
   */
  public Sound getSound(final String name) {
    assert keyMap.containsKey(name);

    Sound s = soundMap.get(name);
    if (s==null) {
      InputStream in = cl.getResourceAsStream(keyMap.get(name));
      assert in!=null;
      s = new Sound(in);
      soundMap.put(name,s);
    }
    
    return s;
  }
  
  /**
   * Load an image by its resource index name.
   * 
   * @param name
   *          the name  (key) of the image
   * @return the image itself
   */
  private BufferedImage loadImage(final String name) {
  	assert keyMap.containsKey(name) : name;
  	
    try {
      // Read the image as a buffered image using ImageIO.
    	URL url = cl.getResource(keyMap.get(name));
    	assert url != null : "Cannot find resource " + keyMap.get(name);
      BufferedImage img = ImageIO.read(url);

      // Create a copy of the image
      int transparency = img.getColorModel().getTransparency();
      BufferedImage copy = gc.createCompatibleImage(img.getWidth(), img
          .getHeight(), transparency);
      
      // Draw the copy, which forces it to be initialized and ready to draw.
      // Note that this should now be a managed image (and hopefully in VRAM).
      Graphics2D g = copy.createGraphics();
      g.drawImage(img,0,0,null);
      g.dispose();
      
      // Return the now-initialized-and-managed copy of the original image.
      return copy;
      
    } catch (java.io.IOException e) {
    	e.printStackTrace();
    	return null;
    }
  }
  
  
  /**
   * Initialize this resource loader.
   * This will load and process the resource index file.
   * After parsing, the {@link #keyMap} will contain the logical names
   * (keys) and the resource locations for all resources.
   * 
   * @param in the input stream containing the resource index (in xml)
   */
  private static void init(InputStream in) {
    try {
      DocumentBuilder builder = 
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(in);
      new IndexParser(document).parse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Responsible for parsing the resource index.
   * 
   * @author pvg
   */
  private final static class IndexParser {
    private final Document document;
    public IndexParser(Document document) { this.document=document; }
    public void parse() {
      NodeList children = document.getChildNodes();
      for (int i=0; i<children.getLength(); i++) {
        Node n = children.item(i);
        if (n.getNodeName().equalsIgnoreCase(DTD.RESOURCE_LIST))
          parseResourceList((Element)n);
      }
    }
    
    private void parseResourceList(Element e) {
      assert e.getNodeName().equalsIgnoreCase(DTD.RESOURCE_LIST);
      NodeList children = e.getChildNodes();
      for (int i=0; i<children.getLength(); i++) {
        Node child = children.item(i);
        String name = child.getNodeName();
        if (name.equalsIgnoreCase(DTD.IMAGE)) 
          parseImage((Element)child);
        else if (name.equalsIgnoreCase(DTD.MUSIC))
          parseMusic((Element)child);
        else if (name.equalsIgnoreCase(DTD.SOUND))
          parseSound((Element)child);
      }
    }
    
    /**
     * Common for parsing leaves with name and resource attributes.
     * @param e
     */
    private void parseLeaf(Element e) {
      String key = e.getAttribute(DTD.NAME_ATT);
      String resource = e.getAttribute(DTD.RESOURCE_ATT);
      assert key!=null;
      assert resource!=null;
      assert !keyMap.containsKey(key);
      
      keyMap.put(key, resource);
    }
    
    private void parseImage(Element e) {
      parseLeaf(e);
    }
    
    private void parseMusic(Element e) {
      parseLeaf(e);
    }
    
    private void parseSound(Element e) {
      parseLeaf(e);
    }
  }
}
