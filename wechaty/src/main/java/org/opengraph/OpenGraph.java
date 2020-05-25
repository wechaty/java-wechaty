package org.opengraph;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Java object representation of an Open Graph enabled webpage.
 * A simplified layer over a Hastable.
 *
 * @author Callum Jones
 */
public class OpenGraph
{
    private String pageUrl;
    private ArrayList<OpenGraphNamespace> pageNamespaces;
    private Hashtable<String, ArrayList<MetaElement>> metaAttributes;
    private String baseType;
    private boolean isImported; // determine if the object is a new incarnation or representation of a web page
    private boolean hasChanged; // track if object has been changed

    public final static String[] REQUIRED_META = new String[]{"title", "type", "image", "url" };

    public final static Hashtable<String, String[]> BASE_TYPES = new Hashtable<String, String[]>();
    static
    {
        BASE_TYPES.put("activity", new String[] {"activity", "sport"});
        BASE_TYPES.put("business", new String[] {"bar", "company", "cafe", "hotel", "restaurant"});
        BASE_TYPES.put("group", new String[] {"cause", "sports_league", "sports_team"});
        BASE_TYPES.put("organization", new String[] {"band", "government", "non_profit", "school", "university"});
        BASE_TYPES.put("person", new String[] {"actor", "athlete", "author", "director", "musician", "politician", "profile", "public_figure"});
        BASE_TYPES.put("place", new String[] {"city", "country", "landmark", "state_province"});
        BASE_TYPES.put("product", new String[] {"album", "book", "drink", "food", "game", "movie", "product", "song", "tv_show"});
        BASE_TYPES.put("website", new String[] {"blog", "website", "article"});
    }

    /**
     * Create an open graph representation for generating your own Open Graph object
     */
    public OpenGraph()
    {
        pageNamespaces = new ArrayList<OpenGraphNamespace>();
        metaAttributes = new Hashtable<String, ArrayList<MetaElement>>();
        hasChanged = false;
        isImported = false;
    }

    /**
     * Fetch the open graph representation from a web site
     * @param url The address to the web page to fetch Open Graph data
     * @param ignoreSpecErrors Set this option to true if you don't wish to have an exception throw if the page does not conform to the basic 4 attributes
     * @throws java.io.IOException If a network error occurs, the HTML parser will throw an IO Exception
     * @throws java.lang.Exception A generic exception is throw if the specific page fails to conform to the basic Open Graph standard as define by the constant REQUIRED_META
     */
    public OpenGraph(String url, boolean ignoreSpecErrors) throws java.io.IOException, Exception {
        this();
        isImported = true;


        // download the (X)HTML content, but only up to the closing head tag. We do not want to waste resources parsing irrelevant content
        URL pageURL = new URL(url);
        URLConnection siteConnection = pageURL.openConnection();
        Charset charset = getConnectionCharset(siteConnection);
        BufferedReader dis = new BufferedReader(new InputStreamReader(siteConnection.getInputStream(), charset));
        String inputLine;
        StringBuffer headContents = new StringBuffer();

        // Loop through each line, looking for the closing head element
        while ((inputLine = dis.readLine()) != null)
        {
            if (inputLine.contains("</head>"))
            {
                inputLine = inputLine.substring(0, inputLine.indexOf("</head>") + 7);
                inputLine = inputLine.concat("<body></body></html>");
                headContents.append(inputLine + "\r\n");
                break;
            }
            headContents.append(inputLine + "\r\n");
        }

        String headContentsStr = headContents.toString();
        HtmlCleaner cleaner = new HtmlCleaner();
        // parse the string HTML
        TagNode pageData = cleaner.clean(headContentsStr);

        // read in the declared namespaces
        boolean hasOGspec = false;
        TagNode headElement = pageData.findElementByName("head", true);
        if (headElement.hasAttribute("prefix"))
        {
            String namespaceData = headElement.getAttributeByName("prefix");
            Pattern pattern = Pattern.compile("(([A-Za-z0-9_]+):\\s+(http:\\/\\/ogp.me\\/ns(\\/\\w+)*#))\\s*");
            Matcher matcher = pattern.matcher(namespaceData);
            while (matcher.find())
            {
                String prefix = matcher.group(2);
                String documentURI = matcher.group(3);
                pageNamespaces.add(new OpenGraphNamespace(prefix, documentURI));
                if (prefix.equals("og"))
                    hasOGspec = true;
            }
        }

        // some pages do not include the new OG spec
        // this fixes compatibility
        if (!hasOGspec)
            pageNamespaces.add(new OpenGraphNamespace("og", "http:// ogp.me/ns#"));

        // open only the meta tags
        TagNode[] metaData = pageData.getElementsByName("meta", true);
        for (TagNode metaElement : metaData)
        {
            for (OpenGraphNamespace namespace : pageNamespaces)
            {
                String target = null;
                if (metaElement.hasAttribute("property"))
                    target = "property";
                else if (metaElement.hasAttribute("name"))
                    target = "name";

                if (target != null && metaElement.getAttributeByName(target).startsWith(namespace.getPrefix() + ":"))
                {
                    setProperty(namespace, metaElement.getAttributeByName(target), metaElement.getAttributeByName("content"));
                    break;
                }
            }
        }

        /**
         * Check that page conforms to Open Graph protocol
         */
        if (!ignoreSpecErrors)
        {
            for (String req : REQUIRED_META)
            {
                if (!metaAttributes.containsKey(req))
                    throw new Exception("Does not conform to Open Graph protocol");
            }
        }

        /**
         * Has conformed, now determine basic sub type.
         */
        baseType = null;
        String currentType = getContent("type");
        // some apps use their OG namespace as a prefix
        if (currentType != null)
        {
            for (OpenGraphNamespace ns : pageNamespaces)
            {
                if (currentType.startsWith(ns.getPrefix() + ":"))
                {
                    currentType = currentType.replaceFirst(ns.getPrefix() + ":","");
                    break; // done here
                }
            }
        }
        for (String base : BASE_TYPES.keySet())
        {
            String[] baseList = BASE_TYPES.get(base);
            boolean finished = false;
            for (String expandedType : baseList)
            {
                if (expandedType.equals(currentType))
                {
                    baseType = base;
                    finished = true;
                    break;
                }
            }
            if (finished) break;
        }

        // read the original page url
        URL realURL = siteConnection.getURL();
        pageUrl = realURL.toExternalForm();
    }

    /**
     * Gets the charset for specified connection.
     * Content Type header is parsed to get the charset name.
     *
     * @param connection the connection.
     * @return the Charset object for response charset name;
     *         if it's not found then the default charset.
     */
    private static Charset getConnectionCharset(URLConnection connection)
    {
        String contentType = connection.getContentType();
        if (contentType != null && contentType.length() > 0)
        {
            contentType = contentType.toLowerCase();
            String charsetName = extractCharsetName(contentType);
            if (charsetName != null && charsetName.length() > 0)
            {
                try
                {
                    return Charset.forName(charsetName);
                }
                catch (Exception e) {
                    // specified charset is not found,
                    // skip it to return the default one
                }
            }
        }

        // return the default charset
        return Charset.defaultCharset();
    }

    /**
     * Extract the charset name form the content type string.
     * Content type string is received from Content-Type header.
     *
     * @param contentType the content type string, must be not null.
     * @return the found charset name or null if not found.
     */
    private static String extractCharsetName(String contentType)
    {
        // split onto media types
        final String[] mediaTypes = contentType.split(":");
        if (mediaTypes.length > 0)
        {
            // use only the first one, and split it on parameters
            final String[] params = mediaTypes[0].split(";");

            // find the charset parameter and return it's value
            for (String each : params)
            {
                each = each.trim();
                if (each.startsWith("charset="))
                {
                    // return the charset name
                    return each.substring(8).trim();
                }
            }
        }

        return null;
    }

    /**
     * Get the basic type of the Open graph page as per the specification
     * @return Base type as defined by specification, null otherwise
     */
    public String getBaseType()
    {
        return baseType;
    }

    /**
     * Get a value of a given Open Graph property
     * @param property The Open graph property key
     * @return Returns the value of the first property defined, null otherwise
     */
    public String getContent(String property)
    {
        if (metaAttributes.containsKey(property) && metaAttributes.get(property).size() > 0)
            return metaAttributes.get(property).get(0).getContent();
        else
            return null;
    }

    /**
     * Get all the defined properties of the Open Graph object
     * @return An array of all currently defined properties
     */
    public MetaElement[] getProperties()
    {
        ArrayList<MetaElement> allElements = new ArrayList<MetaElement>();
        for (ArrayList<MetaElement> collection : metaAttributes.values())
            allElements.addAll(collection);

        return (MetaElement[]) allElements.toArray(new MetaElement[allElements.size()]);
    }

    /**
     * Get all the defined properties of the Open Graph object
     * @param property The property to focus on
     * @return An array of all currently defined properties
     */
    public MetaElement[] getProperties(String property)
    {
        if (metaAttributes.containsKey(property))
        {
            ArrayList target = metaAttributes.get(property);
            return (MetaElement[]) target.toArray(new MetaElement[target.size()]);
        }
        else
            return null;
    }

    /**
     * Get the original URL the Open Graph page was obtained from
     * @return The address to the Open Graph object page
     */
    public String getOriginalUrl()
    {
        return pageUrl;
    }


    /**
     * Get the HTML representation of the Open Graph data.
     * @return An array of meta elements as Strings
     */
    public String[] toHTML()
    {
        // allocate the array
        ArrayList<String> returnHTML = new ArrayList<String>();

        int index = 0; // keep track of the index to insert into
        for (ArrayList<MetaElement> elements : metaAttributes.values())
        {
            for (MetaElement element : elements)
                returnHTML.add("<meta property=\"" + element.getNamespace() + ":" +
                    element.getProperty() + "\" content=\"" + element.getContent() + "\" />");
        }

        // return the array
        return (String[]) returnHTML.toArray();
    }

    /**
     * Get the XHTML representation of the Open Graph data.
     * @return An array of meta elements as Strings
     */
    public String[] toXHTML()
    {
        // allocate the array
        ArrayList<String> returnHTML = new ArrayList<String>();

        int index = 0; // keep track of the index to insert into
        for (ArrayList<MetaElement> elements : metaAttributes.values())
        {
            for (MetaElement element : elements)
                returnHTML.add("<meta name=\"" + element.getNamespace().getPrefix() + ":" +
                    element.getProperty() + "\" content=\"" + element.getContent() + "\" />");
        }

        // return the array
        return (String[]) returnHTML.toArray();
    }

    /**
     * Set the Open Graph property to a specific value
     * @param namespace The OpenGraph namespace the content belongs to
     * @param property The og:XXXX where XXXX is the property you wish to set
     * @param content The value or contents of the property to be set
     */
    public void setProperty(OpenGraphNamespace namespace, String property, String content)
    {
        if (!pageNamespaces.contains(namespace))
            pageNamespaces.add(namespace);

        property = property.replaceAll(namespace.getPrefix() + ":", "");
        MetaElement element = new MetaElement(namespace, property, content);
        if (!metaAttributes.containsKey(property))
            metaAttributes.put(property, new ArrayList<MetaElement>());

        metaAttributes.get(property).add(element);
    }

    /**
     * Removed a defined property
     * @param property The og:XXXX where XXXX is the property you wish to remove
     */
    public void removeProperty(String property)
    {
        metaAttributes.remove(property);
    }

    /**
     * Obtain the underlying HashTable
     * @return The underlying structure as a Hashtable
     */
    public Hashtable<String, ArrayList<MetaElement>> exposeTable() {
        return metaAttributes;
    }

    /**
     * Test if the Open Graph object was initially a representation of a web page
     * @return True if the object is from a web page, false otherwise
     */
    public boolean isFromWeb()
    {
        return isImported;
    }

    /**
     * Test if the object has been modified by setters/deleters.
     * This is only relevant if this object initially represented a web page
     * @return True True if the object has been modified, false otherwise
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }
}
