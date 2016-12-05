package venturas.mtcp.packets;

import java.io.Serializable;

// FLags ae just an enumerated type!
public enum Flag implements Serializable {
	SYN, //as in TCP
	ACK, //acknowledge
	MIG, // initiating migs
	MIG_READY, //Tell that we are ready
	REQ_STATE, //request app state
	RSP_STATE, //responding with state to a request for state
	MESSAGE, //just basic data message
	ADVISE_MIG //server is overloaded? But client must initiate migration => Server can ADVISE this initation
}
