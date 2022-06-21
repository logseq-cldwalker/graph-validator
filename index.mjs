import { addClassPath, loadFile } from '@logseq/nbb-logseq';
import { resolve, dirname } from 'path';
import * as actionsCore from '@actions/core';
import * as actionsGithub from '@actions/github';

const __dirname = dirname(".");
const theFile = resolve(__dirname, 'action.cljs');
addClassPath(resolve(__dirname, 'node_modules/@chr15m/sitefox/src'));
// addClassPath(resolve(__dirname, 'node_modules/@logseq/graph-parser/src'));
// addClassPath(resolve(__dirname, 'node_modules/@logseq/db/src'));
const { action } = await loadFile(theFile);
action( { actionsCore, actionsGithub} );
