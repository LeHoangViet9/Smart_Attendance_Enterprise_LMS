import re

file_path = 'src/main/java/edufit_com_lms/module/quiz/service/impl/AIGradingServiceImpl.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Using byte string or ASCII to find the match
lines = content.split('\n')
for i, line in enumerate(lines):
    if '.feedback("' in line and 'AI' in line and 'tuy' in line:
        lines[i] = '                    .feedback("This is a simulated AI feedback. The answer is quite good but needs deeper analysis.")'

with open(file_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))
