const labelGroups = {
  schedule: ['P0', 'P1', 'P2', 'P3'],
  severity: ['Critical', 'High', 'Medium', 'Low'],
};

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function readIssueFormValue(body, heading) {
  const pattern = new RegExp(
    `^###[ \\t]+${escapeRegExp(heading)}[ \\t]*\\r?\\n` +
      `(?:[ \\t]*\\r?\\n)*([^\\r\\n]+)`,
    'm',
  );
  const match = body.match(pattern);

  return match?.[1].trim();
}

function collectLabels(body) {
  const schedule = readIssueFormValue(body, '📅 처리 시기');
  const severity = readIssueFormValue(body, '🚨 중요도');

  return new Set([
    ...(labelGroups.schedule.includes(schedule) ? [schedule] : []),
    ...(labelGroups.severity.includes(severity) ? [severity] : []),
  ]);
}

module.exports = async ({ github, context }) => {
  const issue = context.payload.issue;
  if (!issue || issue.pull_request) {
    return;
  }

  const { owner, repo } = context.repo;
  const selectedLabels = collectLabels(issue.body || '');
  const managedLabels = Object.values(labelGroups).flat();
  const currentLabels = issue.labels.map((label) =>
    typeof label === 'string' ? label : label.name,
  );

  const labelsToAdd = [...selectedLabels].filter(
    (label) => !currentLabels.includes(label),
  );
  const labelsToRemove = currentLabels.filter(
    (label) => managedLabels.includes(label) && !selectedLabels.has(label),
  );

  if (labelsToAdd.length > 0) {
    await github.rest.issues.addLabels({
      owner,
      repo,
      issue_number: issue.number,
      labels: labelsToAdd,
    });
  }

  for (const label of labelsToRemove) {
    await github.rest.issues.removeLabel({
      owner,
      repo,
      issue_number: issue.number,
      name: label,
    }).catch((error) => {
      if (error.status !== 404) {
        throw error;
      }
    });
  }
};

module.exports.collectLabels = collectLabels;
module.exports.readIssueFormValue = readIssueFormValue;
