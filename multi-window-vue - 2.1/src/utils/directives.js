export default {
  mounted(el, binding) {
    el.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        // 忽略 textarea 的 Enter 键，允许换行
        if (e.target.tagName === 'TEXTAREA') {
          return;
        }

        // 查找所有可聚焦的输入框 (input, textarea)
        // 排除 disabled, hidden, readonly, type=hidden, type=file
        const inputs = Array.from(el.querySelectorAll('input, textarea'))
          .filter(input => 
            !input.disabled && 
            !input.hidden && 
            input.style.display !== 'none' &&
            input.type !== 'hidden' && 
            input.type !== 'file'
          );
        
        const activeElement = document.activeElement;
        const index = inputs.indexOf(activeElement);
        
        if (index > -1) {
          e.preventDefault(); // 阻止默认提交行为
          
          if (index < inputs.length - 1) {
            // 聚焦下一个输入框
            inputs[index + 1].focus();
          } else {
            // 最后一个输入框，执行回调函数（通常是提交表单）
            if (typeof binding.value === 'function') {
              binding.value();
            } else {
              activeElement.blur();
            }
          }
        }
      }
    });
  }
};
