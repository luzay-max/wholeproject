export const createClickGuard = (defaultCooldown = 800) => {
  const pendingKeys = new Set();
  const lastRunAt = new Map();

  const run = async (key, action, options = {}) => {
    const guardKey = String(key);
    const cooldown = Number(options.cooldown ?? defaultCooldown);
    const now = Date.now();

    if (pendingKeys.has(guardKey)) {
      return { executed: false, reason: 'pending' };
    }

    const last = lastRunAt.get(guardKey) || 0;
    if (now - last < cooldown) {
      return { executed: false, reason: 'cooldown' };
    }

    lastRunAt.set(guardKey, now);
    pendingKeys.add(guardKey);
    try {
      const value = await action();
      return { executed: true, value };
    } finally {
      pendingKeys.delete(guardKey);
    }
  };

  return { run };
};

