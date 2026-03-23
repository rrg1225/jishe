import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

export function useUserInfo() {
  const router = useRouter()
  const userRole = ref(localStorage.getItem('userRole') || 'student')
  const userName = ref('加载中...')
  const userAccount = ref('加载中...') 
  const userOrg = ref('加载中...') 
  const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')
  const isDropdownOpen = ref(false)

  const toggleDropdown = () => isDropdownOpen.value = !isDropdownOpen.value
  const closeDropdown = () => isDropdownOpen.value = false

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
        userOrg.value = userRole.value === 'enterprise' ? (res.companyName || res.org) : (res.schoolName || res.org)
        userAvatar.value = res.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'
      }
    } catch (error) {
      fallbackToLocalData()
    }
  }

  const fallbackToLocalData = () => {
    userRole.value = localStorage.getItem('userRole') || 'student'
    userOrg.value = localStorage.getItem('userOrg') || '未知机构'
    userName.value = localStorage.getItem('userName') || '未命名用户'
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
    userRole, userName, userAccount, userOrg, userAvatar,
    isDropdownOpen, toggleDropdown, closeDropdown, fetchHeaderUserInfo, logout
  }
}