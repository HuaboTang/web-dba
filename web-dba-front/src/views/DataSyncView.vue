<script setup lang="ts">
// env
import { reactive, ref } from 'vue'
import { ApiService } from '@/util/fetch'
import { useRouter } from 'vue-router'

let router = useRouter()
const queryParam = reactive<{ [Key: string]: any } >({
  name: null,
  sourceDataSourceId: null,
  targetDataSourceId: null,
  page: 1,
  rows: 10
})
const total = ref(0)
const dataSources = ref<{
  name: string,
  id: number
}[]>([])
const tableData = ref<any[]>([])
// /env

const loadDataSource = async () => {
  const response = await ApiService.getInstance().get('/api/datasource/all')
  dataSources.value = response.data.data
}
const loadData = async () => {
  try {
    const response = await ApiService.getInstance().post('/api/data/sync/page', queryParam)
    total.value = response.data.data.total
    tableData.value = response.data.data.rows
  } catch (e) {
    console.log(e)
  }
}

const onAddDataSyncJob = () => {
  console.log('onAddDataSyncJob')
  router.push({ path: '/data/sync/add' })
}

const onEditJob = (row: any) => {
  console.log('onEditJob', row)
  router.push({ path: '/data/sync/add', query: { jobId: row.id } })
}

loadDataSource()
loadData()
</script>

<template>
  <div>
    <el-form :model="queryParam" label-width="100" inline>
      <el-form-item label="名称">
        <el-input v-model="queryParam.name" />
      </el-form-item>
      <el-form-item label="源数据源">
        <el-select v-model="queryParam.sourceDataSourceId"
                   style="width: 200px"
                   placeholder="请选择">
          <el-option
            v-for="item in dataSources"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="目标数据源">
        <el-select v-model="queryParam.targetDataSourceId"
                   style="width: 200px"
                   placeholder="请选择">
          <el-option
            v-for="item in dataSources"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>
    <el-row justify="end">
      <el-button type="primary" @click="onAddDataSyncJob">新增同步任务</el-button>
    </el-row>
    <el-table :data="tableData">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="sourceDataSource.name" label="源数据源" />
      <el-table-column prop="sourceSchema" label="源Schema" />
      <el-table-column prop="sourceTableName" label="源表名" />
      <el-table-column prop="targetDataSource.name" label="目标数据源" />
      <el-table-column prop="targetSchema" label="目标Schema" />
      <el-table-column prop="targetTableName" label="目标表名" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column prop="operation" label="操作">
        <template #default="scope">
          <el-button @click="onEditJob(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="end">
    <el-pagination :current-page="queryParam.page"
                   :page-size="queryParam.rows"
                   layout="total, prev, pager, next, jumper"
                   :total="total"
                   @current-change="loadData"
                   @prev-click="loadData"></el-pagination>
    </el-row>
  </div>
</template>

<style scoped>

</style>