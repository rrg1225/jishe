<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <header class="header">
    <div class="logo">双帆引航</div>

    <div class="search-container">
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input
          type="text"
          placeholder="搜索院校、考研或竞赛..."
          v-model="dash.searchKeyword"
          @focus="dash.showHotSearch = true"
          @blur="dash.hideHotSearch"
          @keyup.enter="dash.performSearch(dash.searchKeyword)"
        />
      </div>

      <div class="hot-search-dropdown" v-show="dash.showHotSearch">
        <div class="hot-search-header">
          <span class="hot-title">🔥 热搜榜</span>
          <span class="update-time">实时更新</span>
        </div>

        <div class="hot-search-list">
          <div
            v-for="(item, index) in dash.hotSearchList"
            :key="index"
            class="hot-search-item"
            @mousedown.prevent="dash.selectHotSearch(item)"
          >
            <span class="rank-number" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
            <span class="hot-icon" v-if="index < 3">🔥</span>
            <span class="hot-text">{{ item.keyword }}</span>
            <span class="hot-count" :class="{ 'top-three-count': index < 3 }">{{ item.count }}万</span>
          </div>
        </div>

        <div class="hot-search-footer" @click="dash.performSearch(dash.searchKeyword || '竞赛')">
          <span>查看更多结果 →</span>
        </div>
      </div>
    </div>

    <div class="header-right">
      <button class="go-publish-btn" @click="dash.router.push('/publish')">
        <span class="icon">✍️</span> 发布内容
      </button>

      <button
        v-if="dash.userRole === 'enterprise'"
        class="post-offer-btn"
        @click="dash.router.push('/post-offer')"
      >
        <span class="icon">📋</span> 发布 岗位
      </button>

      <div class="user-profile" @click="dash.toggleDropdown">
        <span class="role-badge">{{ dash.userRole === 'enterprise' ? '企业 HR' : '学生' }}</span>

        <div class="avatar">
          <img :src="dash.userAvatar" alt="个人头像" />
        </div>

        <div class="dropdown-overlay" v-show="dash.isDropdownOpen" @click.stop="dash.closeDropdown"></div>

        <div class="dropdown-menu" v-show="dash.isDropdownOpen" @click.stop>
          <div class="dropdown-header">
            <div class="username">{{ dash.userName }}</div>
            <div class="user-account">{{ dash.userAccount }}</div>
            <div class="user-org" v-if="dash.userOrg">{{ dash.userOrg }}</div>
          </div>
          <div class="divider"></div>
          <ul class="dropdown-list">
            <li class="dropdown-item" @click.stop="dash.goToProfile">
              <i>👤</i> 个人信息
            </li>
            <li class="dropdown-item" @click.stop="dash.logout">
              <i>🚪</i> 退出登录
            </li>
          </ul>
        </div>
      </div>
    </div>
  </header>
</template>
