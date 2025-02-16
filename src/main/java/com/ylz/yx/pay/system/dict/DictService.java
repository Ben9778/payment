package com.ylz.yx.pay.system.dict;

import com.ylz.yx.pay.system.dict.model.XtZd0000;
import com.ylz.yx.pay.system.dict.query.AddDictParam;
import com.ylz.yx.pay.system.dict.query.QueryParam;
import com.ylz.yx.pay.system.dict.query.UpdDictParam;

import java.util.List;
import java.util.Map;


public interface DictService {

    List<XtZd0000> getDictListByParentId(QueryParam param);

    Object getDictList(QueryParam param);

    void addDict(AddDictParam param);

    void updDict(UpdDictParam updDictParam);

    void switchDictById(QueryParam param);

    void deleteDictById(QueryParam param);

    List getAllDict();

    Map getAllDictInMap();

}
