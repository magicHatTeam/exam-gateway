package com.bosssoft.cloud.gateway;

import java.util.List;

/**
 * 在redis缓存中的菜单实体类
 * @author likang
 * @date 2019/9/11 14:45
 */
public class Menu {
    /**
     * 资源ID
     */
    private Long id;
    /**
     * 资源编码
     */
    private String code;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 打开图标
     */
    private String openImg;
    /**
     * 父节点ID
     */
    private Long parentId;
    /**
     * 状态
     */
    private Byte status;
    /**
     * 访问路由
     */
    private String url;
    /**
     * 子节点集合
     */
    private List children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenImg() {
        return openImg;
    }

    public void setOpenImg(String openImg) {
        this.openImg = openImg;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public List getChildren() {
        return children;
    }

    public void setChildren(List children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "MenuDto{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", openImg='" + openImg + '\'' +
                ", parentId=" + parentId +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", children=" + children +
                '}';
    }
}
