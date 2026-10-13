import { Ionicons } from '@expo/vector-icons';
import React from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View, ViewStyle } from 'react-native';
import { colors, formatDuration, Session, Subject } from '../lib/study';

export type IconName = keyof typeof Ionicons.glyphMap;
export function Icon({ name, size = 22, color = colors.ink }: { name: IconName; size?: number; color?: string }) {
  return <Ionicons name={name} size={size} color={color} />;
}
export function Page({ children, style }: { children: React.ReactNode; style?: ViewStyle }) {
  return <ScrollView style={styles.scroll} contentContainerStyle={[styles.page, style]} showsVerticalScrollIndicator={false}>{children}</ScrollView>;
}
export function SectionHeading({ title, action, onAction }: { title: string; action?: string; onAction?: () => void }) {
  return <View style={styles.sectionHead}><Text style={styles.sectionTitle}>{title}</Text>{action && <Pressable onPress={onAction}><Text style={styles.sectionAction}>{action}</Text></Pressable>}</View>;
}
export function StatCard({ icon, label, value, tint = '#FDE9E2' }: { icon: IconName; label: string; value: string; tint?: string }) {
  return <View style={styles.statCard}><View style={[styles.statIcon, { backgroundColor: tint }]}><Icon name={icon} size={19} color={colors.ink} /></View><Text style={styles.statValue}>{value}</Text><Text style={styles.statLabel}>{label}</Text></View>;
}
export function SubjectBadge({ subject, size = 42 }: { subject?: Subject; size?: number }) {
  return <View style={{ width: size, height: size, borderRadius: size * .34, backgroundColor: subject?.color ?? '#D5D9DE', alignItems: 'center', justifyContent: 'center' }}><Icon name={(subject?.icon as IconName) ?? 'book-outline'} color={colors.white} size={size * .49} /></View>;
}
export function SessionRow({ session, subject, onLongPress }: { session: Session; subject?: Subject; onLongPress?: () => void }) {
  const start = new Date(session.startedAt);
  return <Pressable onLongPress={onLongPress} style={styles.sessionRow}><SubjectBadge subject={subject} /><View style={{ flex: 1, marginLeft: 12 }}><Text style={styles.sessionName}>{subject?.name ?? 'Deleted subject'}</Text><Text style={styles.sessionTime}>{start.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })} · {start.toLocaleDateString([], { month: 'short', day: 'numeric' })}</Text></View><Text style={styles.sessionDuration}>{formatDuration(session.durationMs)}</Text></Pressable>;
}
export function EmptyState({ icon, title, message }: { icon: IconName; title: string; message: string }) {
  return <View style={styles.empty}><View style={styles.emptyIcon}><Icon name={icon} size={27} color={colors.orange} /></View><Text style={styles.emptyTitle}>{title}</Text><Text style={styles.emptyMessage}>{message}</Text></View>;
}
export const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: colors.background },
  page: { paddingHorizontal: 22, paddingTop: 24, paddingBottom: 38 },
  sectionHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 15 },
  sectionTitle: { fontSize: 19, fontWeight: '800', color: colors.ink, letterSpacing: -.4 },
  sectionAction: { fontSize: 13, fontWeight: '700', color: colors.orange },
  statCard: { flex: 1, backgroundColor: colors.white, borderRadius: 22, padding: 17, minHeight: 137, borderWidth: 1, borderColor: '#F0F0ED' },
  statIcon: { width: 34, height: 34, borderRadius: 11, alignItems: 'center', justifyContent: 'center', marginBottom: 13 },
  statValue: { fontSize: 25, fontWeight: '800', color: colors.ink, letterSpacing: -.8 },
  statLabel: { fontSize: 12, color: colors.muted, marginTop: 3, fontWeight: '500' },
  sessionRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.white, padding: 12, borderRadius: 18, marginBottom: 9, borderWidth: 1, borderColor: '#F0F0ED' },
  sessionName: { fontSize: 14, color: colors.ink, fontWeight: '700' },
  sessionTime: { fontSize: 11, color: colors.muted, marginTop: 3 },
  sessionDuration: { fontSize: 13, color: colors.ink, fontWeight: '800' },
  empty: { alignItems: 'center', paddingHorizontal: 30, paddingVertical: 30, backgroundColor: colors.white, borderRadius: 20 },
  emptyIcon: { width: 52, height: 52, borderRadius: 17, backgroundColor: '#FEF0EA', alignItems: 'center', justifyContent: 'center', marginBottom: 10 },
  emptyTitle: { color: colors.ink, fontSize: 15, fontWeight: '800' },
  emptyMessage: { color: colors.muted, fontSize: 12, textAlign: 'center', lineHeight: 18, marginTop: 4 },
});
