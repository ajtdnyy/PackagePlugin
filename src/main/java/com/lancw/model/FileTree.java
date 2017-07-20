/*
 * File:FileTree.java
 * Date:2016-2-27 11:30:56
 * Encoding:UTF-8
 * Author:lancw
 * Description:文件多叉树，用于提升打包搜索文件的效率
 */
package com.lancw.model;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author lancw
 */
public class FileTree implements Serializable {

    private String name;
    private FileTree parent;
    private String absolutePath;
    private Set<FileTree> children = new LinkedHashSet<FileTree>();
    private static final long serialVersionUID = -1466479389299512377L;

    public FileTree(File file, FileTree parent) {
        String fileName = file.getName();
        fileName = fileName == null || fileName.isEmpty() ? file.getPath() : fileName;
        fileName = fileName.replaceAll("\\\\", "");
        if (fileName.equals("root")) {
            this.name = fileName;
            this.absolutePath = fileName;
        } else {
            this.name = fileName;
            this.parent = parent;
            this.absolutePath = fileName.contains(":") ? fileName : file.getAbsolutePath().replaceAll("\\\\", "/");
        }
        this.parent = parent;
    }

    /**
     * 根据路径查找节点
     *
     * @param paths
     * @return
     */
    public FileTree searchByPath(String[] paths) {
        FileTree pTree = this.getParent();
        String pname = pTree == null ? "" : pTree.getName();
        if (this.name.equals(paths[paths.length - 1]) && (pname.equals("root") || pname.equals(paths[paths.length - 2]))) {
            return this;
        } else {
            return searchFileTree(this, paths);
        }
    }

    /**
     * 递归深度优先搜索
     *
     * @param tree
     * @param paths
     * @return
     */
    private FileTree searchFileTree(FileTree tree, String[] paths) {
        FileTree pTree = tree.getParent();
        String pname = pTree == null ? "" : pTree.getName();
        //当前节点名称相关且父级节点也相同时认为是同一文件
        if (tree.getName().equals(paths[paths.length - 1]) && (pname.equals("root") || pname.equals(paths[paths.length - 2]))) {
            return tree;
        }
        for (FileTree fileTree : tree.getChildren()) {
            FileTree tmp = searchFileTree(fileTree, paths);//递归调用searchFileTree
            if (tmp != null) {//找到节点时返回
                return tmp;
            }
        }
        return null;
    }

    /**
     * 添加子节点，将文件路径存储成树结构
     *
     * @param file
     * @return 子节点
     */
    public FileTree addChild(File file) {
        String[] paths = file.getAbsolutePath().replaceAll("\\\\", "/").split("/");
        FileTree tree = null;//存储最新的子节点
        String path = "";
        for (String path1 : paths) {
            path += path1 + "/";
            FileTree tmpTree = searchByPath(path.split("/"));//查找目录是否已经存在树中
            if (tmpTree == null) {//当前目录或文件不存在时创建新的节点，父节点为root节点即this
                tmpTree = new FileTree(new File(path), tree == null ? this : tree);
                if (tree == null) {//新子节点为空时将新的子节点存在root子节点集合中
                    this.children.add(tmpTree);
                } else {
                    tree.children.add(tmpTree);
                }
                tree = tmpTree;//更新新的子节点
            } else {
                tree = tmpTree;
            }
        }
        return this;
    }

    /**
     * 覆盖equals，集合存储时判断是否为相同节点要用到
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileTree) {
            FileTree ft = (FileTree) obj;
            return this.absolutePath.equals(ft.getAbsolutePath());
        }
        return false;
    }

    /**
     * 覆盖hashCode，集合存储时判断是否为相同节点要用到
     *
     * @return
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return absolutePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileTree getParent() {
        return parent;
    }

    public void setParent(FileTree parent) {
        this.parent = parent;
    }

    public boolean isIsLeaf() {
        return children.isEmpty();
    }

    public Set<FileTree> getChildren() {
        return children;
    }

    public void setChildren(Set<FileTree> children) {
        this.children = children;
    }
}
