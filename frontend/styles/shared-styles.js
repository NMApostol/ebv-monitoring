// eagerly import theme styles so as we can override them
import '@vaadin/vaadin-lumo-styles/all-imports';

const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `
<custom-style>
  <style>
    html {
      --lumo-font-size: 1rem;
      --lumo-font-size-xxxl: 3rem;
      --lumo-font-size-xxl: 2.25rem;
      --lumo-font-size-xl: 1.75rem;
      --lumo-font-size-l: 1.375rem;
      --lumo-font-size-m: 1.125rem;
      --lumo-font-size-s: 1rem;
      --lumo-font-size-xs: 0.875rem;
      --lumo-font-size-xxs: 0.8125rem;
      --lumo-line-height-m: 1.4;
      --lumo-line-height-s: 1.2;
      --lumo-line-height-xs: 1.1;
      --lumo-size-xl: 3rem;
      --lumo-size-l: 2.5rem;
      --lumo-size-m: 2rem;
      --lumo-size-s: 1.75rem;
      --lumo-size-xs: 1.5rem;
      --lumo-space-xl: 1.875rem;
      --lumo-space-l: 1.25rem;
      --lumo-space-m: 0.625rem;
      --lumo-space-s: 0.3125rem;
      --lumo-space-xs: 0.1875rem;
    }
  </style>
</custom-style>


`;

document.head.appendChild($_documentContainer.content);
