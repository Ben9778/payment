package com.ylz.yx.pay.system.channel;

import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.system.channel.model.PayFwqd00RQ;
import com.ylz.yx.pay.system.channel.model.QueryParam;

import java.util.Map;

public interface ChannelService{

    Map<String, Object> queryZfqdList();

    Object detailZfqd(PayZfqd00 payZfqd00);

    void addZfqd(PayZfqd00 payZfqd00);

    void updZfqd(PayZfqd00 payZfqd00);

    void delZfqd(PayZfqd00 payZfqd00);

    Map<String, Object> queryFwqdList(QueryParam param);

    Object detailFwqd(PayFwqd00RQ payFwqd00RQ);

    void addFwqd(PayFwqd00RQ payFwqd00RQ);

    void updFwqd(PayFwqd00RQ payFwqd00RQ);

    void delFwqd(PayFwqd00RQ payFwqd00RQ);

    Object queryZfqdTree(QueryParam param);

    Object getPrivateKey();
}
