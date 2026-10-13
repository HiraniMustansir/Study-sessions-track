import { Platform } from 'react-native';
import type { StudyData } from './study';
import { dayKey, formatDuration, sessionDay, totalMs } from './study';

const IDENTIFIER = 'studyflow-daily-summary';

export async function syncDailySummary(data: StudyData) {
  if (Platform.OS === 'web') return;
  const Notifications = await import('expo-notifications');
  await Notifications.cancelScheduledNotificationAsync(IDENTIFIER).catch(() => undefined);
  if (!data.settings.notificationsEnabled) return;

  const permission = await Notifications.getPermissionsAsync();
  if (!permission.granted) return;
  const now = new Date();
  const reminder = new Date(now.getFullYear(), now.getMonth(), now.getDate(), data.settings.reminderHour, 0);
  if (reminder.getTime() <= now.getTime()) return;

  const today = data.sessions.filter(s => sessionDay(s) === dayKey(now));
  const body = today.length
    ? `You studied for ${formatDuration(totalMs(today))} across ${today.length} ${today.length === 1 ? 'session' : 'sessions'} today. Nice work!`
    : 'No sessions logged yet today. A little study time still counts.';

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('daily-summary', {
      name: 'Daily study summary',
      importance: Notifications.AndroidImportance.DEFAULT,
    });
  }
  await Notifications.scheduleNotificationAsync({
    identifier: IDENTIFIER,
    content: { title: 'Your study day at a glance', body },
    trigger: { type: Notifications.SchedulableTriggerInputTypes.DATE, date: reminder, channelId: 'daily-summary' },
  });
}

export async function requestSummaryPermission() {
  if (Platform.OS === 'web') return false;
  const Notifications = await import('expo-notifications');
  const current = await Notifications.getPermissionsAsync();
  if (current.granted) return true;
  const requested = await Notifications.requestPermissionsAsync();
  return requested.granted;
}

export async function configureNotificationHandler() {
  if (Platform.OS === 'web') return;
  const Notifications = await import('expo-notifications');
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldPlaySound: false,
      shouldSetBadge: false,
      shouldShowBanner: true,
      shouldShowList: true,
    }),
  });
}
