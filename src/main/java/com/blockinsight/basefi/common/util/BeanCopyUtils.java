package com.blockinsight.basefi.common.util;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

public class BeanCopyUtils extends BeanUtils
{

    static
    {
        ConvertUtils.register(new LongConverter(null), Long.class);
        ConvertUtils.register(new ShortConverter(null), Short.class);
        ConvertUtils.register(new IntegerConverter(null), Integer.class);
        ConvertUtils.register(new DoubleConverter(null), Double.class);
        ConvertUtils.register(new BigDecimalConverter(null), BigDecimal.class);
    }

    public static void copyNotNullProperties(Object source, Object target, String[] ignoreProperties) throws BeansException
    {
        copyNotNullProperties(source, target, null, ignoreProperties);
    }


    public static void copyNotNullProperties(Object source, Object target, Class<?> editable) throws BeansException
    {
        copyNotNullProperties(source, target, editable, null);
    }


    public static void copyNotNullProperties(Object source, Object target) throws BeansException
    {
        copyNotNullProperties(source, target, null, null);
    }


    private static void copyNotNullProperties(Object source, Object target, Class<?> editable, String[] ignoreProperties) throws BeansException
    {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();
        if (editable != null)
        {
            if (!editable.isInstance(target))
            {
                throw new IllegalArgumentException("Target class [" + target.getClass().getName() + "] not assignable to Editable class [" + editable.getName() + "]");
            }
            actualEditable = editable;
        }
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;


        for (PropertyDescriptor targetPd : targetPds)
        {
            if (targetPd.getWriteMethod() != null && (ignoreProperties == null || (!ignoreList.contains(targetPd.getName()))))
            {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null && sourcePd.getReadMethod() != null)
                {
                    try
                    {
                        Method readMethod = sourcePd.getReadMethod();
                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()))
                        {
                            readMethod.setAccessible(true);
                        }
                        Object value = readMethod.invoke(source);
                        if (value != null || (readMethod.getReturnType().getName() instanceof String))
                        {// 这里判断以下value是否为空，当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等，如果是String类型，则不需要验证是否为空
                            boolean isEmpty = false;
                            if (value instanceof Set)
                            {
                                Set s = (Set) value;
                                if (s == null || s.isEmpty())
                                {
                                    isEmpty = true;
                                }
                            }
                            else if (value instanceof Map)
                            {
                                Map m = (Map) value;
                                if (m == null || m.isEmpty())
                                {
                                    isEmpty = true;
                                }
                            }
                            else if (value instanceof List)
                            {
                                List l = (List) value;
                                if (l == null || l.size() < 1)
                                {
                                    isEmpty = true;
                                }
                            }
                            else if (value instanceof Collection)
                            {
                                Collection c = (Collection) value;
                                if (c == null || c.size() < 1)
                                {
                                    isEmpty = true;
                                }
                            }
                            if (!isEmpty)
                            {
                                Method writeMethod = targetPd.getWriteMethod();
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()))
                                {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, value);
                            }
                        }
                    }
                    catch (RuntimeException ex)
                    {
                        throw new FatalBeanException("Could not copy properties from source to target", ex);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new FatalBeanException("Could not copy properties from source to target", e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new FatalBeanException("Could not copy properties from source to target", e);
                    }
                }
            }
        }
    }

}
