package org.gpucloudsimplus.listeners;

import org.cloudsimplus.listeners.EventInfo;
import org.cloudbus.cloudsim.gp.videocards.Videocard;

public interface VideocardEventInfo extends EventInfo {

	Videocard getVideocard();
}

