package org.opengraph;

/**
 * Represents an OpenGraph namespace
 * @author Callum Jones
 */
public class OpenGraphNamespace
{
    private String prefix;
    private String schemaURI;

    /**
     * Construct a namespace
     * @param prefix The OpenGraph assigned namespace prefix such as og or og_appname
     * @param schemaURI The URL for the OpenGraph schema
     */
    public OpenGraphNamespace(String prefix, String schemaURI)
    {
        this.prefix = prefix;
        this.schemaURI = schemaURI;
    }

    /*
     * Fetch the prefix used for the namespace
     */
    public String getPrefix()
    {
        return prefix;
    }

    /*
     * Fetch the address for the schema reference
     */
    public String getSchemaURI()
    {
        return schemaURI;
    }
}
