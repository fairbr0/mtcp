package venturas.mtcp.packets;

import java.io.Serializable;

public enum Flag implements Serializable {
	SYN, ACK,
	MIG, MIG_READY,
	REQ_STATE, RSP_STATE,
	MESSAGE
}
