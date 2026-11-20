import re, os
base='/home/claude/lede/app/src/main/java/me/ash/reader/'
S=base+'ui/page/settings/'
pages=[
 ('Interaction','interaction',[],[S+'interaction/InteractionPage.kt',base+'brief/player/PlayerSettings.kt',base+'brief/triage/TriageAuto.kt']),
 ('ColorAndStyle','color_and_style',[],[S+'color/ColorAndStylePage.kt',base+'brief/ui/style/StyleOptions.kt']),
 ('DarkTheme','dark_theme',['color_and_style'],[S+'color/DarkThemePage.kt']),
 ('FeedsPageStyle','feeds_page',['color_and_style'],[S+'color/feeds/FeedsPageStylePage.kt']),
 ('FlowPageStyle','lists_page',['color_and_style'],[S+'color/flow/FlowPageStylePage.kt']),
 ('BottomBar','bottom_bar',['color_and_style'],[base+'brief/ui/style/BottomBarPage.kt']),
 ('ReadingPageStyle','reading_page',['color_and_style'],[S+'color/reading/ReadingStylePage.kt',base+'brief/reading/NewsletterSettingItem.kt']),
 ('ReadingBoldCharacters','bold_characters',['color_and_style','reading_page'],[S+'color/reading/BoldCharactersPage.kt']),
 ('ReadingPageTitle','title',['color_and_style','reading_page'],[S+'color/reading/ReadingTitlePage.kt']),
 ('ReadingPageText','text',['color_and_style','reading_page'],[S+'color/reading/ReadingTextPage.kt']),
 ('ReadingPageImage','images',['color_and_style','reading_page'],[S+'color/reading/ReadingImagePage.kt']),
 ('ReadingPageVideo','videos',['color_and_style','reading_page'],[S+'color/reading/ReadingVideoPage.kt']),
 ('AiModel','ai_settings',[],[base+'brief/ai/AiSettingsPage.kt']),
 ('Filters','filters_settings',[],[base+'brief/filters/FiltersPage.kt']),
 ('ReadingStats','stats_title',[],[base+'brief/stats/ReadingStatsPage.kt']),
 ('Troubleshooting','troubleshooting',[],[S+'troubleshooting/TroubleshootingPage.kt']),
 ('TipsAndSupport','tips_and_support',[],[S+'tips/TipsAndSupportPage.kt']),
 ('Accounts','accounts',[],[S+'accounts/AccountsPage.kt']),
]
# helper-file titles that belong to a different page than the file's usual one
extra={
 'FlowPageStyle':['list_look_shorten'],
 'BottomBar':['bottom_bar_compact'],
 'ColorAndStyle':['glass_refraction','glass_reading','glass_outline'],
 'Accounts':['auto_backup_title','auto_backup_folder','auto_backup_keep','auto_backup_now','backup_everything','restore_from_backup','offline_copies_title'],
}
pat=re.compile(r'title\s*=\s*stringResource\(\s*R\.string\.(\w+)\s*\)(.{0,260}?)(?=\n\s*(?:title|\)\s*\{|\)\n)|$)',re.S)
descpat=re.compile(r'desc\s*=\s*stringResource\(\s*R\.string\.(\w+)\s*\)')
skip={'settings','back','close','cancel','confirm','ok','done'}
out=[]
seen_global=set()
for route,title,path,files in pages:
    seen=set()
    entries=[]
    for f in files:
        if not os.path.exists(f): print('missing',f); continue
        s=open(f,encoding='utf-8').read()
        for m in re.finditer(r'title\s*=\s*stringResource\(\s*R\.string\.(\w+)\s*\)',s):
            name=m.group(1)
            if name in skip or name in seen: continue
            window=s[m.end():m.end()+260]
            d=descpat.search(window)
            # make sure the desc belongs to this call: no other 'title =' in between
            if d and 'title' in window[:d.start()]: d=None
            seen.add(name); entries.append((name,d.group(1) if d else None))
    for name in extra.get(route,[]):
        if name not in seen: seen.add(name); entries.append((name,None))
    out.append((route,title,path,entries))
def q(x): return 'R.string.'+x if x else 'null'
lines=['// Brief You: generated list of settings for the settings search (see tools/settings_index.py).',
'package me.ash.reader.brief.search','','import me.ash.reader.R','import me.ash.reader.ui.page.nav3.key.Route','',
'/** One searchable setting: its name, an optional description, the page it is on, and that page\'s path. */',
'class SettingsEntry(val title: Int, val desc: Int?, val route: Route, val page: Int, val path: List<Int>)','',
'object SettingsIndex {','    val entries: List<SettingsEntry> = listOf(']
# top-level page entries first
for route,title,path,entries in out:
    lines.append(f'        SettingsEntry(R.string.{title}, null, Route.{route}, R.string.{title}, listOf({", ".join("R.string."+p for p in path)})),')
for route,title,path,entries in out:
    full=path+[title]
    for name,desc in entries:
        if name==title: continue
        lines.append(f'        SettingsEntry({q(name)}, {q(desc)}, Route.{route}, R.string.{title}, listOf({", ".join("R.string."+p for p in full)})),')
lines+=['    )','}','']
open(base+'brief/search/SettingsIndex.kt','w',encoding='utf-8').write('\n'.join(lines))
print(sum(len(e) for *_,e in out),'entries')
