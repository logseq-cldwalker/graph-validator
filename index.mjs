import { addClassPath, loadFile } from '@logseq/nbb-logseq';
import { resolve, dirname } from 'path';
import * as actionsCore from '@actions/core';
import * as actionsGithub from '@actions/github';
import {readdirSync} from 'fs';

const __dirname = dirname(".");
const theFile = resolve(__dirname, 'action.cljs');
console.log("DIR", __dirname);
console.log("FILES", readdirSync(__dirname));
console.log("ACTION", theFile);
addClassPath(resolve(__dirname, 'node_modules/@logseq/graph-parser/src'));
addClassPath(resolve(__dirname, 'node_modules/@logseq/db/src'));
const { action } = await loadFile(theFile);
action( { actionsCore, actionsGithub} );
