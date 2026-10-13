import { Ionicons } from '@expo/vector-icons';
import { Slot, usePathname, useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import React, { useEffect } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { configureNotificationHandler } from '../lib/notifications';
import { StudyProvider } from '../lib/store';
import { colors } from '../lib/study';
import { IconName } from '../components/ui';

const tabs: { path: '/' | '/calendar' | '/insights' | '/subjects' | '/settings'; label: string; icon: IconName; activeIcon: IconName }[] = [
  { path: '/', label: 'Today', icon: 'home-outline', activeIcon: 'home' },
  { path: '/calendar', label: 'Calendar', icon: 'calendar-outline', activeIcon: 'calendar' },
  { path: '/insights', label: 'Insights', icon: 'bar-chart-outline', activeIcon: 'bar-chart' },
  { path: '/subjects', label: 'Subjects', icon: 'grid-outline', activeIcon: 'grid' },
  { path: '/settings', label: 'Settings', icon: 'settings-outline', activeIcon: 'settings' },
];

export default function RootLayout() {
  const path = usePathname();
  const router = useRouter();
  useEffect(() => { configureNotificationHandler().catch(() => undefined); }, []);
  return <SafeAreaProvider><StudyProvider><SafeAreaView style={styles.safe} edges={['top', 'bottom']}><StatusBar style="dark" /><View style={styles.content}><Slot /></View><View style={styles.nav}>{tabs.map(tab => {
    const active = path === tab.path;
    return <Pressable key={tab.path} onPress={() => router.push(tab.path)} style={styles.navItem}><View style={[styles.navIcon, active && styles.activeIcon]}><Ionicons name={active ? tab.activeIcon : tab.icon} size={21} color={active ? colors.orange : '#9BA2A9'} /></View><Text style={[styles.navText, active && styles.activeText]}>{tab.label}</Text></Pressable>;
  })}</View></SafeAreaView></StudyProvider></SafeAreaProvider>;
}
const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background }, content: { flex: 1 },
  nav: { flexDirection: 'row', backgroundColor: colors.white, borderTopWidth: 1, borderTopColor: '#EDF0EE', paddingTop: 9, paddingBottom: 4, paddingHorizontal: 5 },
  navItem: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  navIcon: { width: 39, height: 31, alignItems: 'center', justifyContent: 'center', borderRadius: 12 },
  activeIcon: { backgroundColor: '#FFF0EA' }, navText: { color: '#9BA2A9', fontSize: 10, fontWeight: '600', marginTop: 2 }, activeText: { color: colors.orange, fontWeight: '800' },
});
