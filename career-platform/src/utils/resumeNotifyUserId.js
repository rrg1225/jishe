/**
 * 简历异步完成 WebSocket 通道为 /chat/{userId}_notify，须与后端 ai-generate 传入的 userId 一致。
 * 优先使用登录时写入的 userAccount（手机号），避免 userName 被改成昵称后不一致。
 */
export function getResumeNotifyUserId() {
  const id =
    localStorage.getItem('userAccount') ||
    localStorage.getItem('userName') ||
    ''
  return id || 'default_user'
}
