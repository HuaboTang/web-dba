<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="common-layout">
  <el-container>
    <el-aside width="200px" class="main-aside">
      <el-menu router :default-active="$route.path" mode="vertical">
        <el-menu-item index="/">
          <template #title>
            <i class="el-icon-s-home"></i>
            <span>数据源</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/import">
          <template #title>
            <i class="el-icon-s-home"></i>
            <span>数据导入</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/data/sync">
          <template #title>
            <i class="el-icon-s-promotion"></i>
            <span>数据同步</span>
          </template>
        </el-menu-item>
      </el-menu>

    </el-aside>
    <el-main>
      <RouterView />
    </el-main>
  </el-container>
  </div>
</template>

<style scoped>
.main-aside {
  min-height: 100%;
}
</style>