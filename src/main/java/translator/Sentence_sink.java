package translator;

import java.io.File;
import java.util.Properties;

//**********************************************************
public class Sentence_sink
//**********************************************************
{
    Properties the_properties;
    File file_out;

    //**********************************************************
    public Sentence_sink(File file)
    //**********************************************************
    {
        this.file_out = file;
        the_properties = new Properties();
    }
    //**********************************************************
    public void add(String key, String value)
    //**********************************************************
    {
        the_properties.put(key,value);
    }
    //**********************************************************
    public boolean save()
    //**********************************************************
    {
        // save all properties
        try
        {
            the_properties.store(new java.io.FileOutputStream(file_out), "No comment");
            return true;
        }
        catch (java.io.IOException e)
        {
            return false;
        }
    }
}
