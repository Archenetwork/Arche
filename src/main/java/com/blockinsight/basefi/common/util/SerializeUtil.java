package com.blockinsight.basefi.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public final class SerializeUtil
{

    public static byte[] serialize(Object object)
    {
        ObjectOutputStream oos;
        ByteArrayOutputStream baos;
        try
        {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return null;
        }
    }

    public static Object unserialize(byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
        }
        ByteArrayInputStream bais;
        try
        {
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String logExceptionStack(Throwable e)
    {
        StringWriter errorsWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(errorsWriter));
        return errorsWriter.toString();
    }


}
