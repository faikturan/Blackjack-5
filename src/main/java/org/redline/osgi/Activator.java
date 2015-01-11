package org.redline.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by Andrey on 22.11.2014.
 */
public class Activator implements BundleActivator, ServiceListener
{

    public void start(BundleContext context)
    {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("Gambling", "Blackjack");
        context.registerService(DealerInstantiator.class, new DealerInstantiatorImpl(), properties);
        context.addServiceListener(this);
    }


    public void stop(BundleContext context) {}


    public void serviceChanged(ServiceEvent event)
    {
        String[] objectClass = (String[])
                event.getServiceReference().getProperty("objectClass");

        if (event.getType() == ServiceEvent.REGISTERED)
        {
            System.out.println(
                    "Ex1: Service of type " + objectClass[0] + " registered.");
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            System.out.println(
                    "Ex1: Service of type " + objectClass[0] + " unregistered.");
        }
        else if (event.getType() == ServiceEvent.MODIFIED)
        {
            System.out.println(
                    "Ex1: Service of type " + objectClass[0] + " modified.");
        }
    }
}
