package com.ylz.yx.pay.system.role.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TreeStructure implements Serializable {
    private static final long serialVersionUID = -522270956600173413L;

    private String key;

    private String title;

    private String parent;

    private String path;

    private Integer level;

    private String component;

    private String id;

    private boolean checked;

    private boolean disabled;

    private List<TreeStructure> children;

}
