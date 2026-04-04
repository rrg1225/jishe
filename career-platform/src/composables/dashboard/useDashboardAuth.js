import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../utils/request'

/**
 * 顶栏用户态、下拉菜单与「编辑简历」路由联动。
 */
export function useDashboardAuth(router, activeMenu) {
  const route = useRoute()
  const userRole = ref(localStorage.getItem('userRole') || 'student')
  const userName = ref('加载中...')
  const userAccount = ref('加载中...')
  const userOrg = ref('加载中...')
  const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')
  const isDropdownOpen = ref(false)

  const toggleDropdown = () => (isDropdownOpen.value = !isDropdownOpen.value)
  const closeDropdown = () => (isDropdownOpen.value = false)

  const fallbackToLocalData = () => {
    const savedRole = localStorage.getItem('userRole')
    if (savedRole) userRole.value = savedRole
    const savedOrg = localStorage.getItem('userOrg')
    userOrg.value = savedOrg || (userRole.value === 'enterprise' ? '未知企业' : '未知学校')
    const savedName = localStorage.getItem('userName')
    userName.value = savedName || (userRole.value === 'enterprise' ? '企业HR' : '学生用户')
    userAccount.value = '暂无网络'
  }

  const fetchHeaderUserInfo = async () => {
    try {
      const res = await request.get('/api/user/info')
      if (res) {
        userName.value = res.nickname || res.nickName || '未命名用户'
        userAccount.value = res.phone || '未绑定手机号'
        if (res.role) {
          userRole.value = res.role
          localStorage.setItem('userRole', res.role)
        }
        if (userRole.value === 'enterprise') {
          userOrg.value = res.companyName || res.org || '未填写企业名称'
        } else {
          userOrg.value = res.schoolName || res.org || '未填写学校'
        }
        userAvatar.value = res.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'
        localStorage.setItem('userName', userName.value)
        localStorage.setItem('userOrg', userOrg.value)
      }
    } catch {
      fallbackToLocalData()
    }
  }

  watch(
    () => route.query.edit,
    (newVal) => {
      if (newVal === 'true') {
        activeMenu.value = 'profile'
        router.replace({ path: '/dashboard' })
      }
    },
    { immediate: true }
  )

  const goToProfile = () => {
    closeDropdown()
    router.push('/user-profile')
  }

  const logout = () => {
    closeDropdown()
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userName')
    localStorage.removeItem('userOrg')
    router.push('/login')
  }

  return {
    userRole,
    userName,
    userAccount,
    userOrg,
    userAvatar,
    isDropdownOpen,
    toggleDropdown,
    closeDropdown,
    fetchHeaderUserInfo,
    fallbackToLocalData,
    goToProfile,
    logout,
  }
}
