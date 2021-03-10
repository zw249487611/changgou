package com.changgou.search.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.InsertListenPoint;
import com.xpand.starter.canal.annotation.ListenPoint;

@CanalEventListener
public class CanalDateEventListener {
    /**
     * 增加监听：只有增加后的数据
     * rowData.getBeforeColumnsList();删除、修改
     * rowData.getAfterColumnsList();增加、修改
     * @InsertListenPoint
     * @param eventType : 当前操作的类型，增加数据
     * @param rowData : 发生变更的 一行数据
     */

    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println("列名:" + column.getName() + "----变更的数据：" + column.getValue());
        }
    }
    /**
     * 修改监听
     */
    @InsertListenPoint
    public void onEventUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("修改前：列名:" + column.getName() + "----变更的数据：" + column.getValue());
        }
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println("修改后：列名:" + column.getName() + "----变更的数据：" + column.getValue());
        }
    }

    /**
     * 删除监听
     */
    @InsertListenPoint
    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("删除前：列名:" + column.getName() + "----变更的数据：" + column.getValue());
        }

    }


    /**
     * 自定义监听
     */
    @ListenPoint(
            eventType = {CanalEntry.EventType.DELETE, CanalEntry.EventType.UPDATE},//监听类型
            schema = {"changgou_content"}, //指定监听的数据库
            table = {"tb_content"},//指定监听的表
            destination = "example"
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("自定义操作前：列名:" + column.getName() + "----变更的数据：" + column.getValue());
        }
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println("自定义操作后：列名:" + column.getName() + "----变更的数据：" + column.getValue());
        }

    }

}
