package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:57:44 AM
 */
public class PresenceListener implements PacketListener {

    XmppVillein xmppVillein;

    public PresenceListener(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());


        if(presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed) {
            this.xmppVillein.removeStruct(packet.getFrom());
            return;
        }

        if(LinkedProcess.isBareJid(packet.getFrom())) {
            Struct checkStruct = this.xmppVillein.getStruct(packet.getFrom(), XmppVillein.StructType.HOST);
            if (checkStruct == null) {
                HostStruct hostStruct = new HostStruct();
                hostStruct.setFullJid(packet.getFrom());
                hostStruct.setPresence(presence);
                this.xmppVillein.addHostStruct(hostStruct);
            } else {
                checkStruct.setPresence(presence);
            }
        } else if (packet.getFrom().contains("LoPFarm")) {
            Struct checkStruct = this.xmppVillein.getStruct(packet.getFrom(), XmppVillein.StructType.FARM);
            if (checkStruct == null) {
                FarmStruct farmStruct = new FarmStruct();
                farmStruct.setFullJid(packet.getFrom());
                farmStruct.setPresence(presence);
                this.xmppVillein.addFarmStruct(farmStruct);
            } else {
                checkStruct.setPresence(presence);
            }

        } else {
            // ONLY REPRESENT THOSE VMS THAT YOU HAVE SPAWNEDs
            Struct checkStruct = this.xmppVillein.getStruct(packet.getFrom());
            if (checkStruct != null) {
                checkStruct.setPresence(presence);
            }
        }
    }
}
