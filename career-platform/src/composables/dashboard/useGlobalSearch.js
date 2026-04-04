import { ref } from 'vue'
import request from '../../utils/request'

/**
 * 顶栏热搜、全局搜索与结果列表（需注入内容聚合与卡片格式化能力）。
 */
export function useGlobalSearch(activeMenu, allLocalContentData, formatToCards) {
  const hotSearchList = ref([])

  const fetchRealHotSearch = async () => {
    try {
      const res = await request.get('/api/content/hot-search')
      if (res && res.code === 200 && res.data) {
        hotSearchList.value = res.data
      } else if (res && Array.isArray(res.data)) {
        hotSearchList.value = res.data
      } else if (Array.isArray(res)) {
        hotSearchList.value = res
      }
    } catch (error) {
      console.error('获取实时热搜榜单失败', error)
    }
  }

  const showHotSearch = ref(false)
  const searchKeyword = ref('')
  const searchResults = ref([])
  const isSearching = ref(false)

  let blurTimer = null
  const hideHotSearch = () => {
    blurTimer = setTimeout(() => {
      showHotSearch.value = false
    }, 200)
  }

  const selectHotSearch = (item) => {
    searchKeyword.value = item.keyword
    showHotSearch.value = false
    performSearch(item.keyword)
  }

  const performSearch = async (keyword) => {
    if (!keyword || !keyword.trim()) return
    searchKeyword.value = keyword
    showHotSearch.value = false
    activeMenu.value = 'search'
    isSearching.value = true
    searchResults.value = []

    try {
      const res = await request.get('/api/content/search', { params: { keyword } })
      await fetchRealHotSearch()

      let list = []
      if (Array.isArray(res)) list = res
      else if (res && Array.isArray(res.data)) list = res.data
      else if (res && res.data?.content) list = res.data.content

      if (list && list.length > 0) {
        searchResults.value = formatToCards(list)
        isSearching.value = false
        return
      }

      throw new Error('Backend search returned empty')
    } catch {
      const keywordLower = keyword.toLowerCase()
      const filtered = allLocalContentData.value.filter((item) => {
        const titleMatch = item.title && item.title.toLowerCase().includes(keywordLower)
        const authorMatch = item.authorName && item.authorName.toLowerCase().includes(keywordLower)
        const catMatch = item.category && item.category.toLowerCase().includes(keywordLower)
        return titleMatch || authorMatch || catMatch
      })

      setTimeout(() => {
        searchResults.value = formatToCards(filtered)
        isSearching.value = false
      }, 500)
    }
  }

  const cleanupBlurTimer = () => {
    if (blurTimer) clearTimeout(blurTimer)
  }

  return {
    hotSearchList,
    fetchRealHotSearch,
    showHotSearch,
    searchKeyword,
    searchResults,
    isSearching,
    hideHotSearch,
    selectHotSearch,
    performSearch,
    cleanupBlurTimer,
  }
}
