import React from 'react';
import { createRoot } from 'react-dom/client';
import { MantineProvider, createTheme } from '@mantine/core';
import '@fontsource-variable/bricolage-grotesque';
import '@fontsource/ibm-plex-sans/400.css';
import '@fontsource/ibm-plex-sans/600.css';
import '@mantine/core/styles.css';
import App from './App.jsx';

const theme = createTheme({
  primaryColor: 'tinta',
  colors: {
    // azul de caneta esferográfica, a cor das provas
    tinta: ['#eef3ff', '#dbe4f7', '#b8c8ea', '#93abdd', '#7592d2', '#6182cc', '#5578c9', '#4565b2', '#3b59a0', '#2d4a8e'],
  },
  fontFamily: '"IBM Plex Sans", system-ui, sans-serif',
  headings: { fontFamily: '"Bricolage Grotesque Variable", "IBM Plex Sans", sans-serif', fontWeight: '700' },
  defaultRadius: 'md',
});

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="auto">
      <App />
    </MantineProvider>
  </React.StrictMode>,
);
