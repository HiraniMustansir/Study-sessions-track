import React, { useState } from 'react';
import { Alert, Modal, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { EmptyState, Icon, IconName, Page, SubjectBadge } from '../components/ui';
import { useStudy } from '../lib/store';
import { colors, formatDuration, totalMs } from '../lib/study';

const palette = ['#F3A57D', '#82B8AB', '#9A9BCB', '#E5B46C', '#80A9C9', '#CF94A4'];
const icons: IconName[] = ['book-outline', 'calculator-outline', 'flask-outline', 'code-slash-outline', 'language-outline', 'color-palette-outline'];
export default function SubjectsScreen() {
  const { data, addSubject, deleteSubject } = useStudy();
  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [color, setColor] = useState(palette[0]);
  const [icon, setIcon] = useState<IconName>(icons[0]);
  const submit = () => {
    const trimmed = name.trim();
    if (!trimmed) return Alert.alert('Add a name', 'Enter a subject name to continue.');
    if (data.subjects.some(s => s.name.toLowerCase() === trimmed.toLowerCase())) return Alert.alert('Subject already exists', 'Choose a different name.');
    addSubject(trimmed, color, icon);
    setName(''); setOpen(false);
  };
  const confirmDelete = (id: string, subjectName: string) => {
    if (data.timer?.subjectId === id) return Alert.alert('Session in progress', 'Finish or discard the active session before deleting this subject.');
    Alert.alert(`Delete ${subjectName}?`, 'Past sessions will remain in your totals, but their subject name will no longer be shown.', [
      { text: 'Cancel', style: 'cancel' }, { text: 'Delete', style: 'destructive', onPress: () => deleteSubject(id) },
    ]);
  };
  return <Page><Text style={styles.eyebrow}>STAY ORGANIZED</Text><Text style={styles.title}>Your subjects</Text><Text style={styles.subtitle}>Track where your time goes.</Text>
    <View style={styles.topRow}><Text style={styles.count}>{data.subjects.length} {data.subjects.length === 1 ? 'subject' : 'subjects'}</Text><Pressable onPress={() => setOpen(true)} style={styles.addButton}><Icon name="add" color={colors.white} size={18} /><Text style={styles.addText}>Add subject</Text></Pressable></View>
    {data.subjects.length ? data.subjects.map(subject => {
      const sessions = data.sessions.filter(s => s.subjectId === subject.id);
      return <View key={subject.id} style={styles.subjectCard}><SubjectBadge subject={subject} size={51} /><View style={styles.subjectBody}><Text style={styles.subjectName}>{subject.name}</Text><Text style={styles.subjectInfo}>{formatDuration(totalMs(sessions))} total · {sessions.length} {sessions.length === 1 ? 'session' : 'sessions'}</Text></View><Pressable onPress={() => confirmDelete(subject.id, subject.name)} hitSlop={10}><Icon name="trash-outline" size={19} color="#A8AFB5" /></Pressable></View>;
    }) : <EmptyState icon="grid-outline" title="Make your first subject" message="Create a subject to keep each study session organized." />}
    <View style={styles.tip}><Icon name="bulb-outline" color={colors.orange} size={19} /><Text style={styles.tipText}>Tip: Use subjects for courses, topics, or different kinds of self-study.</Text></View>
    <Modal visible={open} transparent animationType="slide" onRequestClose={() => setOpen(false)}><View style={styles.overlay}><View style={styles.sheet}><View style={styles.sheetHandle} /><View style={styles.sheetHead}><Text style={styles.sheetTitle}>New subject</Text><Pressable onPress={() => setOpen(false)}><Icon name="close" size={23} /></Pressable></View><Text style={styles.fieldLabel}>SUBJECT NAME</Text><TextInput value={name} onChangeText={setName} placeholder="e.g. Physics" placeholderTextColor="#AAB0B5" maxLength={28} style={styles.input} autoFocus /><Text style={styles.fieldLabel}>COLOR</Text><View style={styles.options}>{palette.map(item => <Pressable key={item} onPress={() => setColor(item)} style={[styles.colorOption, { backgroundColor: item }, color === item && styles.chosenColor]}>{color === item && <Icon name="checkmark" color={colors.white} size={18} />}</Pressable>)}</View><Text style={styles.fieldLabel}>ICON</Text><View style={styles.options}>{icons.map(item => <Pressable key={item} onPress={() => setIcon(item)} style={[styles.iconOption, icon === item && styles.chosenIcon]}><Icon name={item} color={icon === item ? colors.orange : colors.muted} size={23} /></Pressable>)}</View><Pressable onPress={submit} style={styles.save}><Text style={styles.saveText}>Create subject</Text><Icon name="arrow-forward" color={colors.white} size={19} /></Pressable></View></View></Modal>
  </Page>;
}
const styles = StyleSheet.create({
  eyebrow: { color: colors.orange, fontSize: 10, fontWeight: '800', letterSpacing: 2, marginBottom: 7 }, title: { color: colors.ink, fontSize: 29, fontWeight: '900', letterSpacing: -1 }, subtitle: { color: colors.muted, fontSize: 13, marginTop: 4, marginBottom: 25 }, topRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 15 }, count: { color: colors.ink, fontSize: 16, fontWeight: '800' }, addButton: { flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: colors.orange, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 9 }, addText: { color: colors.white, fontSize: 12, fontWeight: '800' },
  subjectCard: { backgroundColor: colors.white, borderRadius: 19, padding: 14, flexDirection: 'row', alignItems: 'center', marginBottom: 10, borderWidth: 1, borderColor: '#F0F0ED' }, subjectBody: { flex: 1, marginLeft: 13 }, subjectName: { color: colors.ink, fontSize: 15, fontWeight: '800' }, subjectInfo: { color: colors.muted, fontSize: 11, marginTop: 4 }, tip: { marginTop: 15, backgroundColor: '#FFF0E8', borderRadius: 16, padding: 15, flexDirection: 'row', gap: 10, alignItems: 'center' }, tipText: { color: '#9A765F', fontSize: 12, lineHeight: 17, flex: 1 },
  overlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: '#0007' }, sheet: { backgroundColor: colors.white, borderTopLeftRadius: 27, borderTopRightRadius: 27, padding: 23, paddingBottom: 38 }, sheetHandle: { backgroundColor: '#DDE1E0', width: 38, height: 4, borderRadius: 2, alignSelf: 'center', marginBottom: 21 }, sheetHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 25 }, sheetTitle: { color: colors.ink, fontSize: 22, fontWeight: '900' }, fieldLabel: { color: colors.muted, fontSize: 10, fontWeight: '800', letterSpacing: 1.5, marginBottom: 11 }, input: { backgroundColor: colors.background, borderWidth: 1, borderColor: colors.line, borderRadius: 13, paddingHorizontal: 15, height: 48, fontSize: 14, color: colors.ink, marginBottom: 25 }, options: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 25 }, colorOption: { width: 37, height: 37, borderRadius: 13, alignItems: 'center', justifyContent: 'center' }, chosenColor: { borderWidth: 3, borderColor: colors.ink }, iconOption: { width: 40, height: 40, backgroundColor: colors.background, borderRadius: 12, alignItems: 'center', justifyContent: 'center' }, chosenIcon: { backgroundColor: '#FFF0E8', borderWidth: 1, borderColor: '#F7C5AE' }, save: { backgroundColor: colors.orange, borderRadius: 15, height: 52, paddingHorizontal: 16, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }, saveText: { color: colors.white, fontWeight: '800', fontSize: 15 },
});
