package com.ylz.yx.pay.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.data.dao.support.PageModel;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.system.role.model.TreeStructure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommonUtil {

    private final static int THREAD_SIZE = 4;

    /**
     * 递归合并数组为子数组，最后返回第一层（转换成树形结构）
     *
     * @param resultList
     * @param rank
     * @param listMap
     * @return
     */
    public static List<TreeStructure> tranformTreeStructure(List<TreeStructure> resultList, Integer rank, Map<String, List<TreeStructure>> listMap) {

        // 保存当次调用总共合并了多少元素
        int n;
        // 保存当次调用总共合并出来的list
        Map<String, List<TreeStructure>> currentMap = new HashMap<>();
        // 由于按等级从小到大排序，需要从后往前排序
        // 判断该节点是否属于当前循环的等级,不等于则跳出循环
        for (n = resultList.size() - 1; n >= 0 && resultList.get(n).getLevel().equals(rank); n--) {
            // 判断之前的调用是否有返回以该节点的id为key的map，有则设置为children列表。
            if (listMap != null && StringUtils.isNotEmpty(resultList.get(n).getId())) {
                resultList.get(n).setChildren(listMap.get(resultList.get(n).getId()));
            }
            if (StringUtils.isNotEmpty(resultList.get(n).getParent())) {
                // 判断当前节点所属的parent是否已经创建了以该parent为key的键值对，没有则创建新的链表
                currentMap.computeIfAbsent(resultList.get(n).getParent(), k -> new LinkedList<>());
                // 将该节点插入到对应的list的头部
                currentMap.get(resultList.get(n).getParent()).add(0, resultList.get(n));
            }
        }
        if (n < 0) {
            return resultList;
        } else {
            return tranformTreeStructure(resultList.subList(0, n + 1), resultList.get(n).getLevel(), currentMap);
        }
    }

    public static Map<String, Object> getPageData(String queryLanguage, String countLanguage, Map<String, Object> paramMap, JdbcGateway jdbcGateway) {

        if (paramMap.get("pageSize") == null || paramMap.get("pageIndex") == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(paramMap.get("pageSize").toString());
        int pageIndex = Integer.valueOf(paramMap.get("pageIndex").toString());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", jdbcGateway.selectListByPage(queryLanguage, paramMap, pageModel));
        resultMap.put("total", jdbcGateway.selectOne(countLanguage, paramMap));

        return resultMap;
    }
}
