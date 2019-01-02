package com.sendtomoon.eroica.fling.receipt;

import com.sendtomoon.eroica.fling.msg.FlingReceiptMsg;

public interface CommandReceiptService {
	void receipt(final FlingReceiptMsg receiptMsg);
}
