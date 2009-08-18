/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class LopListener implements PacketListener {
    public XmppClient xmppClient;

    public LopListener(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    protected DiscoverInfo getDiscoInfo(String jid) {
        ServiceDiscoveryManager discoManager = this.xmppClient.getDiscoManager();
        try {
            return discoManager.discoverInfo(jid);
        } catch (XMPPException e) {
            XmppClient.LOGGER.warning("XmppException with DiscoveryManager on " + jid + ": " + e.getMessage());
            return null;
        }
    }

    protected boolean isVirtualMachine(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_VM_NAMESPACE);
        else
            return false;
    }

    protected boolean isFarm(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_FARM_NAMESPACE);
        else
            return false;
    }

    protected boolean isRegistry(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_REGISTRY_NAMESPACE);
        else
            return false;
    }
}
